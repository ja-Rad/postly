package com.jarad.postly.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email_unq", columnList = "email", unique = true)
})
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Email may not be blank")
    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters long")
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank(message = "Password may not be blank")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
    @Column(name = "password")
    private String password;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "active_profile")
    private boolean activeProfile;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @OneToOne(mappedBy = "user", orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Profile profile;

    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }

        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRoleFromUserByRoleName(String roleName) {
        roles.stream()
                .filter(r -> r.getName().equals(roleName))
                .findFirst()
                .ifPresent(role -> role.getUsers().remove(this));
    }

    public void removeRoleById(Long roleId) {
        Role role = roles.stream().filter(r -> r.getId().equals(roleId)).findFirst().orElse(null);

        if (role != null) {
            roles.remove(role);
            role.getUsers().remove(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
