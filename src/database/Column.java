package database;

/**
 * Column instance contains information about
 * name and type of column in database
 *
 * @author Arina Fedorovskaya
 */

public class Column {
    private String name;
    private String type;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }
}
