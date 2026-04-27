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

        if (customer.getMobileNumbers() != null){
            customer.getMobileNumbers().forEach(customerMobile -> customerMobile.setId(customerId));
        }

        if (customer.getAddresses() != null){
            customer.getAddresses().forEach(customerAddress -> customerAddress.setId(customerId));
        }

        saveMobileAndAddress(customer);
        return customerId;
    }

    private void saveMobileAndAddress(Customer customer) {
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
                    ps.setString(4, address.getCityId());
                }

                @Override
                public int getBatchSize() {
                    return customer.getAddresses().size();
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

        //fetch customer mobile
        jdbcTemplate.query("SELECT * FROM customer_mobile_number WHERE customer_id = ?", new Object[]{id},(rs,rowNum) ->{
            CustomerMobile mobile = new CustomerMobile();
            mobile.setId(rs.getLong("customer_id"));
            mobile.setMobileNo(rs.getString("mobile_number"));
            customer.getMobileNumbers().add(mobile);
            return null;
        });



        return customer;
    }
}
