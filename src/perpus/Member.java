package perpus;

public class Member extends User {

    public Member(String id, String name, String password) {
        super(id, name, password);
    }

    @Override
    public String getRole() {
        return "Member";
    }
}
