package com.SEprojects.Assignment.repo;

import com.SEprojects.Assignment.model.Address;
import com.SEprojects.Assignment.model.Customer;
import com.SEprojects.Assignment.model.CustomerMobile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class CustomerRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long saveCustomer(Customer customer) {
        String sql = "INSERT INTO customer (name, dob, nic) VALUES (?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection ->{
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, customer.getName());
            preparedStatement.setDate(2, new java.sql.Date(customer.getDob().getTime()));
            preparedStatement.setString(3,customer.getNic());
            return preparedStatement;
        },keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to insert customer, no ID obtained.");
        }

        Long customerId = key.longValue();
        customer.setId(customerId);

        saveRelations(customer);
        return customerId;
    }

    public void addFamilyMembers(Long customerId, List<Long> familyMemberIds) {
        if (familyMemberIds != null && !familyMemberIds.isEmpty()) {
            String sql = "INSERT IGNORE INTO customer_family_member( customer_id, family_member_id ) VALUES (?,?)";
            // Forward link
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, customerId);
                    ps.setLong(2, familyMemberIds.get(i));
                }

                @Override
                public int getBatchSize() {
                    return familyMemberIds.size();
                }
            });
            // Reverse link
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, familyMemberIds.get(i));
                    ps.setLong(2, customerId);
                }

                @Override
                public int getBatchSize() {
                    return familyMemberIds.size();
                }
            });
        }
    }

    public void updateCustomer(Customer customer) {
        String sql = "UPDATE customer SET name = ?, dob = ?, nic = ? WHERE id = ?";
        jdbcTemplate.update(sql, customer.getName(), new java.sql.Date(customer.getDob().getTime()), customer.getNic(), customer.getId());

        // Delete existing relations and re-insert (simple approach for minimal code)
        jdbcTemplate.update("DELETE FROM customer_mobile_number WHERE customer_id = ?", customer.getId());
        jdbcTemplate.update("DELETE FROM customer_address WHERE customer_id = ?", customer.getId());
        jdbcTemplate.update("DELETE FROM customer_family_member WHERE customer_id = ?", customer.getId());

        saveRelations(customer);
    }

    private void saveRelations(Customer customer) {
        //save mobile number
        if (customer.getMobileNumbers() != null && !customer.getMobileNumbers().isEmpty()){
            String sql = "INSERT INTO customer_mobile_number( customer_id, mobile_number ) VALUES (?,?)";
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, customer.getId());
                    ps.setString(2, customer.getMobileNumbers().get(i).getMobileNo());
                }

                @Override
                public int getBatchSize() {
                    return customer.getMobileNumbers().size();
                }
            });
        }

        //save address
        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()){
            String sql = "INSERT INTO customer_address( customer_id, address_line_1,address_line_2,city_id ) VALUES (?,?,?,?)";
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Address address = customer.getAddresses().get(i);
                    ps.setLong(1, customer.getId());
                    ps.setString(2, address.getAddressLine1());
                    ps.setString(3, address.getAddressLine2());
                    ps.setInt(4, address.getCityId());
                }

                @Override
                public int getBatchSize() {
                    return customer.getAddresses().size();
                }
            });
        }

        //save family members (Bidirectional)
        if (customer.getFamilyMembers() != null && !customer.getFamilyMembers().isEmpty()){
            String sql = "INSERT IGNORE INTO customer_family_member( customer_id, family_member_id ) VALUES (?,?)";
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, customer.getId());
                    ps.setLong(2, customer.getFamilyMembers().get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return customer.getFamilyMembers().size();
                }
            });

            // Add reverse relationship
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, customer.getFamilyMembers().get(i).getId());
                    ps.setLong(2, customer.getId());
                }

                @Override
                public int getBatchSize() {
                    return customer.getFamilyMembers().size();
                }
            });
        }
    }

    public Customer findCustomerById(Long id) {
        String sql = "SELECT * FROM customer WHERE id = ?";
        List<Customer> customers = jdbcTemplate.query(sql, new Object[]{id}, (rs, rowNum) -> {
            Customer c = new Customer();
            c.setId(rs.getLong("id"));
            c.setName(rs.getString("name"));
            c.setDob(rs.getDate("dob"));
            c.setNic(rs.getString("nic"));
            return c;
        });

        if (customers.isEmpty()) return null;
        Customer customer  = customers.get(0);

        fetchRelations(customer);

        return customer;
    }

    private void fetchRelations(Customer customer) {
        //fetch customer mobile
        jdbcTemplate.query("SELECT * FROM customer_mobile_number WHERE customer_id = ?", new Object[]{customer.getId()},(rs,rowNum) ->{
            CustomerMobile mobile = new CustomerMobile();
            mobile.setId(rs.getLong("id"));
            mobile.setMobileNo(rs.getString("mobile_number"));
            customer.getMobileNumbers().add(mobile);
            return null;
        });

        //fetch customer address
        jdbcTemplate.query("SELECT ca.*, ci.name as city_name, co.name as country_name FROM customer_address ca " +
                "JOIN city ci ON ca.city_id = ci.id " +
                "JOIN country co ON ci.country_id = co.id " +
                "WHERE ca.customer_id = ?", new Object[]{customer.getId()},(rs,rowNum) ->{
            Address address = new Address();
            address.setId(rs.getLong("id"));
            address.setAddressLine1(rs.getString("address_line_1"));
            address.setAddressLine2(rs.getString("address_line_2"));
            address.setCityId(rs.getInt("city_id"));
            address.setCityName(rs.getString("city_name"));
            address.setCountryName(rs.getString("country_name"));
            customer.getAddresses().add(address);
            return null;
        });

        //fetch family members (only basic info to avoid recursion)
        jdbcTemplate.query("SELECT c.* FROM customer c JOIN customer_family_member cfm ON c.id = cfm.family_member_id WHERE cfm.customer_id = ?", new Object[]{customer.getId()},(rs,rowNum) ->{
            Customer fm = new Customer();
            fm.setId(rs.getLong("id"));
            fm.setName(rs.getString("name"));
            fm.setDob(rs.getDate("dob"));
            fm.setNic(rs.getString("nic"));
            customer.getFamilyMembers().add(fm);
            return null;
        });
    }

    public List<Customer> findAllCustomers() {
        String sql = "SELECT * FROM customer";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Customer c = new Customer();
            c.setId(rs.getLong("id"));
            c.setName(rs.getString("name"));
            c.setDob(rs.getDate("dob"));
            c.setNic(rs.getString("nic"));
            return c;
        });
    }

    public Long findIdByNic(String nic) {
        String sql = "SELECT id FROM customer WHERE nic = ?";
        List<Long> ids = jdbcTemplate.query(sql, new Object[]{nic}, (rs, rowNum) -> rs.getLong("id"));
        return ids.isEmpty() ? null : ids.get(0);
    }

    public boolean existsByNic(String nic) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customer WHERE nic = ?", new Object[]{nic}, Integer.class);
        return count != null && count > 0;
    }

    public boolean existsByNicAndIdNot(String nic, Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customer WHERE nic = ? AND id != ?", new Object[]{nic, id}, Integer.class);
        return count != null && count > 0;
    }

}
