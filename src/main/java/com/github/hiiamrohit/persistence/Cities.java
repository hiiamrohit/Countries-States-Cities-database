/* 
 * Copyright (C) 2017 Javier A. Ortiz Bultron javier.ortiz.78@gmail.com - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.github.hiiamrohit.persistence;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Javier A. Ortiz Bultrón javier.ortiz.78@gmail.com
 */
@Entity
@Table(name = "cities")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Cities.findAll", query = "SELECT c FROM Cities c"),
    @NamedQuery(name = "Cities.findById", query = "SELECT c FROM Cities c WHERE c.id = :id"),
    @NamedQuery(name = "Cities.findByName", query = "SELECT c FROM Cities c WHERE c.name = :name")})
public class Cities implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "name")
    private String name;
    @JoinColumn(name = "state_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private States stateId;

  /**
   *
   */
  public Cities() {
    }

  /**
   *
   * @param id
   */
  public Cities(Integer id) {
        this.id = id;
    }

  /**
   *
   * @param id
   * @param name
   */
  public Cities(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

  /**
   *
   * @return
   */
  public Integer getId() {
        return id;
    }

  /**
   *
   * @param id
   */
  public void setId(Integer id) {
        this.id = id;
    }

  /**
   *
   * @return
   */
  public String getName() {
        return name;
    }

  /**
   *
   * @param name
   */
  public void setName(String name) {
        this.name = name;
    }

  /**
   *
   * @return
   */
  public States getStateId() {
        return stateId;
    }

  /**
   *
   * @param stateId
   */
  public void setStateId(States stateId) {
        this.stateId = stateId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        
        if (!(object instanceof Cities)) {
            return false;
        }
        Cities other = (Cities) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "games.jwrestling.server.game.db.world.persistence.Cities[ id=" + id + " ]";
    }

}
