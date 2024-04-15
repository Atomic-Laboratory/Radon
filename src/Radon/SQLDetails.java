package Radon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mindustry.Vars;

import java.util.Objects;

@SuppressWarnings("unused")
public class SQLDetails {
    private String driver = "com.mysql.cj.jdbc.Driver";
    private String dialect = "org.hibernate.dialect.MySQL8Dialect";
    private String url = "jdbc:mysql://localhost:3306/Mindustry?rewriteBatchedStatements=true";
    private String user = "username";
    private String password = "password";
    private String HBM2DDL_AUTO = "update";
    private int batch_size = 1024;

    public SQLDetails() {
    }

    public SQLDetails(String driver, String dialect, String url, String user, String password, String HBM2DDL_AUTO, int batch_size) {
        this.driver = driver;
        this.dialect = dialect;
        this.url = url;
        this.user = user;
        this.password = password;
        this.HBM2DDL_AUTO = HBM2DDL_AUTO;
        this.batch_size = batch_size;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHBM2DDL_AUTO() {
        return HBM2DDL_AUTO;
    }

    public void setHBM2DDL_AUTO(String HBM2DDL_AUTO) {
        this.HBM2DDL_AUTO = HBM2DDL_AUTO;
    }

    public int getBatch_size() {
        return batch_size;
    }

    public void setBatch_size(int batch_size) {
        this.batch_size = batch_size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLDetails that = (SQLDetails) o;
        return getBatch_size() == that.getBatch_size() && Objects.equals(getDriver(), that.getDriver()) && Objects.equals(getDialect(), that.getDialect()) && Objects.equals(getUrl(), that.getUrl()) && Objects.equals(getUser(), that.getUser()) && Objects.equals(getPassword(), that.getPassword()) && Objects.equals(getHBM2DDL_AUTO(), that.getHBM2DDL_AUTO());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDriver(), getDialect(), getUrl(), getUser(), getPassword(), getHBM2DDL_AUTO(), getBatch_size());
    }

    public static void saveDefault() {
        var configFolder = Vars.modDirectory.child("Radon/");
        configFolder.mkdirs();
        var configFi = configFolder.child("config.json");
        try {
            configFi.writeString(new ObjectMapper().writeValueAsString(new SQLDetails()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
