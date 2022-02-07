package io.testcontainers.containers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.platform.commons.annotation.Testable;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testable
public class PostgreSQLContainerWithScriptTest {
    @Rule
    public JdbcDatabaseContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:9.4")
            .withInitScript("import.sql");

    @Test
    public void when_select_query_executed_then_return_result() throws SQLException {

        ResultSet resultSet = executeQuery(postgreSQLContainer,
                "SELECT * FROM person where name LIKE '%Donato Di Betto Bardi%'");
        resultSet.next();
        int id = resultSet.getInt(1);
        assertEquals(4, id);
    }

    private ResultSet executeQuery(JdbcDatabaseContainer postgreSQLContainer, String query) throws SQLException {
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();
        String username = postgreSQLContainer.getUsername();
        String password = postgreSQLContainer.getPassword();

        Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
        return conn.createStatement().executeQuery(query);
    }
}
