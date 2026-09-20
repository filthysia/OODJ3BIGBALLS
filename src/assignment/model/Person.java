package assignment.model;

/** Base class for every human actor in the Hospital Management System. */
public abstract class Person {

    protected String id;
    protected String name;
    protected String ic;          // IC / passport number
    protected Gender gender;
    protected String phone;
    protected String email;
    protected String address;
    protected String password;

    protected Person() { }

    protected Person(String id, String name, String ic, Gender gender,
                     String phone, String email, String address, String password) {
        this.id = id;
        this.name = name;
        this.ic = ic;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.password = password;
    }

    /** Human-readable role label, e.g. "Doctor" or "Medical Manager". */
    public abstract String getRole();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIc() { return ic; }
    public void setIc(String ic) { this.ic = ic; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return String.format("%-8s %-24s %-16s %s", id, name, getRole(), email);
    }
}
