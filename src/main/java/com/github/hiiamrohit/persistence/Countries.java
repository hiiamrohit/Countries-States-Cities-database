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
@Table(name = "countries")
@XmlRootElement
@NamedQueries(
        {
          @NamedQuery(name = "Countries.findAll", query = "SELECT c FROM Countries c")
          ,@NamedQuery(name = "Countries.findById",
                  query = "SELECT c FROM Countries c WHERE c.id = :id")
          ,@NamedQuery(name = "Countries.findBySortname",
                  query = "SELECT c FROM Countries c WHERE c.sortname = :sortname")
          ,@NamedQuery(name = "Countries.findByName",
                  query = "SELECT c FROM Countries c WHERE c.name = :name")
        })
public class Countries implements Serializable
{

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "id")
  private Integer id;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "sortname")
  private String sortname;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 150)
  @Column(name = "name")
  private String name;
  @Basic(optional = false)
  @NotNull
  @Size(max = 11)
  @Column(name = "phonecode")
  private int phonecode;
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "countryId")
  private List<States> statesList;

  /**
   *
   */
  public Countries()
  {
  }

  /**
   *
   * @param id
   */
  public Countries(Integer id)
  {
    this.id = id;
  }

  /**
   *
   * @param id
   * @param sortname
   * @param name
   */
  public Countries(Integer id, String sortname, String name)
  {
    this.id = id;
    this.sortname = sortname;
    this.name = name;
  }

  /**
   *
   * @return
   */
  public Integer getId()
  {
    return id;
  }

  /**
   *
   * @param id
   */
  public void setId(Integer id)
  {
    this.id = id;
  }

  /**
   *
   * @return
   */
  public String getSortname()
  {
    return sortname;
  }

  /**
   *
   * @param sortname
   */
  public void setSortname(String sortname)
  {
    this.sortname = sortname;
  }

  /**
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   *
   * @return
   */
  @XmlTransient
  public List<States> getStatesList()
  {
    return statesList;
  }

  /**
   *
   * @param statesList
   */
  public void setStatesList(List<States> statesList)
  {
    this.statesList = statesList;
  }

  @Override
  public int hashCode()
  {
    int hash = 0;
    hash += (id != null ? id.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object)
  {

    if (!(object instanceof Countries))
    {
      return false;
    }
    Countries other = (Countries) object;
    return !((this.id == null && other.id != null) || (this.id != null
            && !this.id.equals(other.id)));
  }

  @Override
  public String toString()
  {
    return "games.jwrestling.server.game.db.world.persistence.Countries[ id=" + id + " ]";
  }

  /**
   * @return the phonecode
   */
  public int getPhonecode()
  {
    return phonecode;
  }

  /**
   * @param phonecode the phonecode to set
   */
  public void setPhonecode(int phonecode)
  {
    this.phonecode = phonecode;
  }

}
