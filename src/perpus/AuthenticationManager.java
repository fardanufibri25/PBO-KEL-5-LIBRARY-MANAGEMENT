package perpus;

import java.util.List;

public class AuthenticationManager {
    private final Admin admin;
    private final List<Member> members;

    public AuthenticationManager(List<Member> members) {
        this.admin = new Admin("admin", "Administrator", "admin123");
        this.members = members;
    }

    public User login(String id, String password) {
        if (admin.getId().equalsIgnoreCase(id) && admin.getPassword().equals(password)) {
            return admin;
        }
        for (Member member : members) {
            if (member.getId().equalsIgnoreCase(id) && member.getPassword().equals(password)) {
                return member;
            }
        }
        return null;
    }
}
