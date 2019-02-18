package com.ustadmobile.lib.database.jdbc;

import org.sqlite.SQLiteConfig;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import java.util.Properties;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Use the underlying driver to initialize a connection pool and bind it if needed
 */
public class DriverConnectionPoolInitializer {

    private static void createSubcontext(InitialContext initialContext, String subcontext) throws NamingException{
        boolean jdbcSubcontextExists = false;
        try {
            NamingEnumeration<Binding> list = initialContext.listBindings(subcontext);
            list.close();
            jdbcSubcontextExists = true;
        }catch(NamingException e) {

        }

        if(!jdbcSubcontextExists){
            initialContext.createSubcontext(subcontext);
        }
    }

    public static void bindDataSource(String dbName, String jdbcUrl, boolean isMaster) {
        DataSource dataSource = null;

        try {
            InitialContext context = new InitialContext();
            String dbJndiName = "java:/comp/env/jdbc/" + dbName;
            String isMasterJndiName = "java:/comp/env/umdb/" + dbName + "/isMaster";
            createSubcontext(context, "java:/comp/env/jdbc");
            createSubcontext(context, "java:/comp/env/umdb");
            createSubcontext(context, "java:/comp/env/umdb/" + dbName);

            try {
                dataSource = (DataSource)context.lookup(dbJndiName);
            }catch(NamingException e) {
                //does not exist
            }

            if(dataSource == null){
                if(jdbcUrl.startsWith("jdbc:sqlite")) {
                    Properties connectionProps = new Properties();
                    connectionProps.setProperty(SQLiteConfig.Pragma.BUSY_TIMEOUT.pragmaName,
                            "10000");
                    dataSource = new SQLiteConnectionPoolDataSource(
                            new SQLiteConfig(connectionProps));

                    ((SQLiteConnectionPoolDataSource) dataSource).setUrl(jdbcUrl);
                    ((SQLiteConnectionPoolDataSource) dataSource).setJournalMode("WAL");
                    context.bind(dbJndiName, dataSource);
                }
            }

            Boolean masterVal = null;
            try {
                masterVal = (Boolean)context.lookup(isMasterJndiName);
            }catch(NamingException e) {

            }
            if(masterVal == null) {
                context.bind(isMasterJndiName, isMaster);
            }
        }catch(NamingException e) {
            throw new RuntimeException("Could not setup initial database context", e);
        }
    }
}
