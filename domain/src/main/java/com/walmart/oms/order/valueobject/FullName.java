package com.walmart.oms.order.valueobject;

import com.walmart.common.domain.AssertionConcern;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class FullName extends AssertionConcern implements Serializable {

  @Column(name = "TITLE")
  private String title;

  @Column(name = "FIRST_NAME")
  private String firstName;

  @Column(name = "MIDDLE_NAME")
  private String middleName;

  @Column(name = "LAST_NAME")
  private String lastName;

  public FullName(String title, String firstName, String middleName, String lastName) {
    this.assertArgumentNotEmpty(firstName, "First Name cannot be empty");
    this.title = title;
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;
  }

  @Override
  public String toString() {
    return "FullName{"
        + "title='"
        + title
        + '\''
        + ", firstName='"
        + firstName
        + '\''
        + ", middleName='"
        + middleName
        + '\''
        + ", lastName='"
        + lastName
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FullName fullName = (FullName) o;
    return Objects.equals(title, fullName.title)
        && firstName.equals(fullName.firstName)
        && Objects.equals(middleName, fullName.middleName)
        && lastName.equals(fullName.lastName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, firstName, middleName, lastName);
  }
}
