package com.walmart.common.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

@MappedSuperclass
@Getter
@NoArgsConstructor
public class BaseEntity extends AssertionConcern implements Serializable {

  @Id
  @Column(name = "RECORD_ID")
  protected String id;

  @Column(name = "DB_LOCK_VERSION")
  @Version
  protected Long version;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "MODIFIED_DATE")
  @ColumnTransformer(
      write = "COALESCE(CURRENT_TIMESTAMP, ?)") // Custom-SQL specifier for insert/update operations
  protected Date modifiedDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(
      name = "CREATED_DATE",
      updatable =
          false) // This annotation makes sure that this column is untouched in update queries (So
  // it's used only in insert queries)
  @ColumnTransformer(
      write = "COALESCE(CURRENT_TIMESTAMP, ?)") // Custom-SQL specifier for insert/update operations
  protected Date createdDate;

  protected BaseEntity(String id) {
    this.id = id;
  }
}
