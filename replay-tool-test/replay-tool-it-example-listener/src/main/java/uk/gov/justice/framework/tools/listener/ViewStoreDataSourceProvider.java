package uk.gov.justice.framework.tools.listener;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ApplicationScoped
public class ViewStoreDataSourceProvider {

    private static final String DATA_SOURCE_JNDI_NAME = "java:/DS.replay";


    private Context initialContext;
    private DataSource datasource = null;


    public ViewStoreDataSourceProvider() throws NamingException {
        initialContext = getInitialContext();
    }

    public Context getInitialContext() throws NamingException {
        if (initialContext == null) {
            initialContext = new InitialContext();
        }
        return initialContext;
    }

    public DataSource getDataSource() {
        if (datasource == null) {
            try {
                datasource = (DataSource) getInitialContext().lookup(DATA_SOURCE_JNDI_NAME);
            } catch (final NamingException e) {
                throw new JdbcRepositoryException(e);
            }
        }
        return datasource;
    }
}
