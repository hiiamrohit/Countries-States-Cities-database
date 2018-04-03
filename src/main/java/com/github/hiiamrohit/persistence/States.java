/* 
 * Copyright (C) 2017 Javier A. Ortiz Bultron javier.ortiz.78@gmail.com - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.github.hiiamrohit.persistence;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Javier A. Ortiz Bultrón javier.ortiz.78@gmail.com
 */
@Entity
@Table(name = "states")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "States.findAll", query = "SELECT s FROM States s"),
    @NamedQuery(name = "States.findById", query = "SELECT s FROM States s WHERE s.id = :id"),
    @NamedQuery(name = "States.findByName", query = "SELECT s FROM States s WHERE s.name = :name")})
public class States implements Serializable {

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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "stateId")
    private List<Cities> citiesList;
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Countries countryId;

  /**
   *
   */
  public States() {
    }

  /**
   *
   * @param id
   */
  public States(Integer id) {
        this.id = id;
    }

  /**
   *
   * @param id
   * @param name
   */
  public States(Integer id, String name) {
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
  @XmlTransient
    public List<Cities> getCitiesList() {
        return citiesList;
    }

  /**
   *
   * @param citiesList
   */
  public void setCitiesList(List<Cities> citiesList) {
        this.citiesList = citiesList;
    }

  /**
   *
   * @return
   */
  public Countries getCountryId() {
        return countryId;
    }

  /**
   *
   * @param countryId
   */
  public void setCountryId(Countries countryId) {
        this.countryId = countryId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        
        if (!(object instanceof States)) {
            return false;
        }
        States other = (States) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "games.jwrestling.server.game.db.world.persistence.States[ id=" + id + " ]";
    }

}
