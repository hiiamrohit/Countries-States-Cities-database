/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiiamrohit.persistence.controller;

import java.io.Serializable;

import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.github.hiiamrohit.persistence.States;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.github.hiiamrohit.persistence.Countries;
import com.github.hiiamrohit.persistence.controller.exceptions.IllegalOrphanException;
import com.github.hiiamrohit.persistence.controller.exceptions.NonexistentEntityException;

/**
 *
 * @author Javier Ortiz Bultron <javierortiz@pingidentity.com>
 */
public class CountriesJpaController implements Serializable
{
  public CountriesJpaController(EntityManagerFactory emf)
  {
    this.emf = emf;
  }
  private EntityManagerFactory emf = null;

  public EntityManager getEntityManager()
  {
    return emf.createEntityManager();
  }

  public void create(Countries countries)
  {
    if (countries.getStatesList() == null)
    {
      countries.setStatesList(new ArrayList<States>());
    }
    EntityManager em = null;
    try
    {
      em = getEntityManager();
      em.getTransaction().begin();
      List<States> attachedStatesList = new ArrayList<States>();
      for (States statesListStatesToAttach : countries.getStatesList())
      {
        statesListStatesToAttach = em.getReference(statesListStatesToAttach.getClass(), statesListStatesToAttach.getId());
        attachedStatesList.add(statesListStatesToAttach);
      }
      countries.setStatesList(attachedStatesList);
      em.persist(countries);
      for (States statesListStates : countries.getStatesList())
      {
        Countries oldCountryIdOfStatesListStates = statesListStates.getCountryId();
        statesListStates.setCountryId(countries);
        statesListStates = em.merge(statesListStates);
        if (oldCountryIdOfStatesListStates != null)
        {
          oldCountryIdOfStatesListStates.getStatesList().remove(statesListStates);
          oldCountryIdOfStatesListStates = em.merge(oldCountryIdOfStatesListStates);
        }
      }
      em.getTransaction().commit();
    }
    finally
    {
      if (em != null)
      {
        em.close();
      }
    }
  }

  public void edit(Countries countries) throws IllegalOrphanException, NonexistentEntityException, Exception
  {
    EntityManager em = null;
    try
    {
      em = getEntityManager();
      em.getTransaction().begin();
      Countries persistentCountries = em.find(Countries.class, countries.getId());
      List<States> statesListOld = persistentCountries.getStatesList();
      List<States> statesListNew = countries.getStatesList();
      List<String> illegalOrphanMessages = null;
      for (States statesListOldStates : statesListOld)
      {
        if (!statesListNew.contains(statesListOldStates))
        {
          if (illegalOrphanMessages == null)
          {
            illegalOrphanMessages = new ArrayList<String>();
          }
          illegalOrphanMessages.add("You must retain States " + statesListOldStates + " since its countryId field is not nullable.");
        }
      }
      if (illegalOrphanMessages != null)
      {
        throw new IllegalOrphanException(illegalOrphanMessages);
      }
      List<States> attachedStatesListNew = new ArrayList<States>();
      for (States statesListNewStatesToAttach : statesListNew)
      {
        statesListNewStatesToAttach = em.getReference(statesListNewStatesToAttach.getClass(), statesListNewStatesToAttach.getId());
        attachedStatesListNew.add(statesListNewStatesToAttach);
      }
      statesListNew = attachedStatesListNew;
      countries.setStatesList(statesListNew);
      countries = em.merge(countries);
      for (States statesListNewStates : statesListNew)
      {
        if (!statesListOld.contains(statesListNewStates))
        {
          Countries oldCountryIdOfStatesListNewStates = statesListNewStates.getCountryId();
          statesListNewStates.setCountryId(countries);
          statesListNewStates = em.merge(statesListNewStates);
          if (oldCountryIdOfStatesListNewStates != null && !oldCountryIdOfStatesListNewStates.equals(countries))
          {
            oldCountryIdOfStatesListNewStates.getStatesList().remove(statesListNewStates);
            oldCountryIdOfStatesListNewStates = em.merge(oldCountryIdOfStatesListNewStates);
          }
        }
      }
      em.getTransaction().commit();
    }
    catch (Exception ex)
    {
      String msg = ex.getLocalizedMessage();
      if (msg == null || msg.length() == 0)
      {
        Integer id = countries.getId();
        if (findCountries(id) == null)
        {
          throw new NonexistentEntityException("The countries with id " + id + " no longer exists.");
        }
      }
      throw ex;
    }
    finally
    {
      if (em != null)
      {
        em.close();
      }
    }
  }

  public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException
  {
    EntityManager em = null;
    try
    {
      em = getEntityManager();
      em.getTransaction().begin();
      Countries countries;
      try
      {
        countries = em.getReference(Countries.class, id);
        countries.getId();
      }
      catch (EntityNotFoundException enfe)
      {
        throw new NonexistentEntityException("The countries with id " + id + " no longer exists.", enfe);
      }
      List<String> illegalOrphanMessages = null;
      List<States> statesListOrphanCheck = countries.getStatesList();
      for (States statesListOrphanCheckStates : statesListOrphanCheck)
      {
        if (illegalOrphanMessages == null)
        {
          illegalOrphanMessages = new ArrayList<String>();
        }
        illegalOrphanMessages.add("This Countries (" + countries + ") cannot be destroyed since the States " + statesListOrphanCheckStates + " in its statesList field has a non-nullable countryId field.");
      }
      if (illegalOrphanMessages != null)
      {
        throw new IllegalOrphanException(illegalOrphanMessages);
      }
      em.remove(countries);
      em.getTransaction().commit();
    }
    finally
    {
      if (em != null)
      {
        em.close();
      }
    }
  }

  public List<Countries> findCountriesEntities()
  {
    return findCountriesEntities(true, -1, -1);
  }

  public List<Countries> findCountriesEntities(int maxResults, int firstResult)
  {
    return findCountriesEntities(false, maxResults, firstResult);
  }

  private List<Countries> findCountriesEntities(boolean all, int maxResults, int firstResult)
  {
    EntityManager em = getEntityManager();
    try
    {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      cq.select(cq.from(Countries.class));
      Query q = em.createQuery(cq);
      if (!all)
      {
        q.setMaxResults(maxResults);
        q.setFirstResult(firstResult);
      }
      return q.getResultList();
    }
    finally
    {
      em.close();
    }
  }

  public Countries findCountries(Integer id)
  {
    EntityManager em = getEntityManager();
    try
    {
      return em.find(Countries.class, id);
    }
    finally
    {
      em.close();
    }
  }

  public int getCountriesCount()
  {
    EntityManager em = getEntityManager();
    try
    {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      Root<Countries> rt = cq.from(Countries.class);
      cq.select(em.getCriteriaBuilder().count(rt));
      Query q = em.createQuery(cq);
      return ((Long) q.getSingleResult()).intValue();
    }
    finally
    {
      em.close();
    }
  }

}
