package uk.gov.justice.framework.tools.replay.events;

import java.util.Objects;
import java.util.UUID;

public class User {

    private UUID userId;
    private String firstName;
    private String lastName;

    public User(final UUID userId, final String firstName, final String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    protected User() {
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final User user = (User) o;
        return Objects.equals(userId, user.userId) &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userId, firstName, lastName);
    }
}
