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

import com.github.hiiamrohit.persistence.Countries;
import com.github.hiiamrohit.persistence.Cities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.github.hiiamrohit.persistence.States;
import com.github.hiiamrohit.persistence.controller.exceptions.IllegalOrphanException;
import com.github.hiiamrohit.persistence.controller.exceptions.NonexistentEntityException;

/**
 *
 * @author Javier Ortiz Bultron <javierortiz@pingidentity.com>
 */
public class StatesJpaController implements Serializable
{
  public StatesJpaController(EntityManagerFactory emf)
  {
    this.emf = emf;
  }
  private EntityManagerFactory emf = null;

  public EntityManager getEntityManager()
  {
    return emf.createEntityManager();
  }

  public void create(States states)
  {
    if (states.getCitiesList() == null)
    {
      states.setCitiesList(new ArrayList<Cities>());
    }
    EntityManager em = null;
    try
    {
      em = getEntityManager();
      em.getTransaction().begin();
      Countries countryId = states.getCountryId();
      if (countryId != null)
      {
        countryId = em.getReference(countryId.getClass(), countryId.getId());
        states.setCountryId(countryId);
      }
      List<Cities> attachedCitiesList = new ArrayList<Cities>();
      for (Cities citiesListCitiesToAttach : states.getCitiesList())
      {
        citiesListCitiesToAttach = em.getReference(citiesListCitiesToAttach.getClass(), citiesListCitiesToAttach.getId());
        attachedCitiesList.add(citiesListCitiesToAttach);
      }
      states.setCitiesList(attachedCitiesList);
      em.persist(states);
      if (countryId != null)
      {
        countryId.getStatesList().add(states);
        countryId = em.merge(countryId);
      }
      for (Cities citiesListCities : states.getCitiesList())
      {
        States oldStateIdOfCitiesListCities = citiesListCities.getStateId();
        citiesListCities.setStateId(states);
        citiesListCities = em.merge(citiesListCities);
        if (oldStateIdOfCitiesListCities != null)
        {
          oldStateIdOfCitiesListCities.getCitiesList().remove(citiesListCities);
          oldStateIdOfCitiesListCities = em.merge(oldStateIdOfCitiesListCities);
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

  public void edit(States states) throws IllegalOrphanException, NonexistentEntityException, Exception
  {
    EntityManager em = null;
    try
    {
      em = getEntityManager();
      em.getTransaction().begin();
      States persistentStates = em.find(States.class, states.getId());
      Countries countryIdOld = persistentStates.getCountryId();
      Countries countryIdNew = states.getCountryId();
      List<Cities> citiesListOld = persistentStates.getCitiesList();
      List<Cities> citiesListNew = states.getCitiesList();
      List<String> illegalOrphanMessages = null;
      for (Cities citiesListOldCities : citiesListOld)
      {
        if (!citiesListNew.contains(citiesListOldCities))
        {
          if (illegalOrphanMessages == null)
          {
            illegalOrphanMessages = new ArrayList<String>();
          }
          illegalOrphanMessages.add("You must retain Cities " + citiesListOldCities + " since its stateId field is not nullable.");
        }
      }
      if (illegalOrphanMessages != null)
      {
        throw new IllegalOrphanException(illegalOrphanMessages);
      }
      if (countryIdNew != null)
      {
        countryIdNew = em.getReference(countryIdNew.getClass(), countryIdNew.getId());
        states.setCountryId(countryIdNew);
      }
      List<Cities> attachedCitiesListNew = new ArrayList<Cities>();
      for (Cities citiesListNewCitiesToAttach : citiesListNew)
      {
        citiesListNewCitiesToAttach = em.getReference(citiesListNewCitiesToAttach.getClass(), citiesListNewCitiesToAttach.getId());
        attachedCitiesListNew.add(citiesListNewCitiesToAttach);
      }
      citiesListNew = attachedCitiesListNew;
      states.setCitiesList(citiesListNew);
      states = em.merge(states);
      if (countryIdOld != null && !countryIdOld.equals(countryIdNew))
      {
        countryIdOld.getStatesList().remove(states);
        countryIdOld = em.merge(countryIdOld);
      }
      if (countryIdNew != null && !countryIdNew.equals(countryIdOld))
      {
        countryIdNew.getStatesList().add(states);
        countryIdNew = em.merge(countryIdNew);
      }
      for (Cities citiesListNewCities : citiesListNew)
      {
        if (!citiesListOld.contains(citiesListNewCities))
        {
          States oldStateIdOfCitiesListNewCities = citiesListNewCities.getStateId();
          citiesListNewCities.setStateId(states);
          citiesListNewCities = em.merge(citiesListNewCities);
          if (oldStateIdOfCitiesListNewCities != null && !oldStateIdOfCitiesListNewCities.equals(states))
          {
            oldStateIdOfCitiesListNewCities.getCitiesList().remove(citiesListNewCities);
            oldStateIdOfCitiesListNewCities = em.merge(oldStateIdOfCitiesListNewCities);
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
        Integer id = states.getId();
        if (findStates(id) == null)
        {
          throw new NonexistentEntityException("The states with id " + id + " no longer exists.");
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
      States states;
      try
      {
        states = em.getReference(States.class, id);
        states.getId();
      }
      catch (EntityNotFoundException enfe)
      {
        throw new NonexistentEntityException("The states with id " + id + " no longer exists.", enfe);
      }
      List<String> illegalOrphanMessages = null;
      List<Cities> citiesListOrphanCheck = states.getCitiesList();
      for (Cities citiesListOrphanCheckCities : citiesListOrphanCheck)
      {
        if (illegalOrphanMessages == null)
        {
          illegalOrphanMessages = new ArrayList<String>();
        }
        illegalOrphanMessages.add("This States (" + states + ") cannot be destroyed since the Cities " + citiesListOrphanCheckCities + " in its citiesList field has a non-nullable stateId field.");
      }
      if (illegalOrphanMessages != null)
      {
        throw new IllegalOrphanException(illegalOrphanMessages);
      }
      Countries countryId = states.getCountryId();
      if (countryId != null)
      {
        countryId.getStatesList().remove(states);
        countryId = em.merge(countryId);
      }
      em.remove(states);
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

  public List<States> findStatesEntities()
  {
    return findStatesEntities(true, -1, -1);
  }

  public List<States> findStatesEntities(int maxResults, int firstResult)
  {
    return findStatesEntities(false, maxResults, firstResult);
  }

  private List<States> findStatesEntities(boolean all, int maxResults, int firstResult)
  {
    EntityManager em = getEntityManager();
    try
    {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      cq.select(cq.from(States.class));
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

  public States findStates(Integer id)
  {
    EntityManager em = getEntityManager();
    try
    {
      return em.find(States.class, id);
    }
    finally
    {
      em.close();
    }
  }

  public int getStatesCount()
  {
    EntityManager em = getEntityManager();
    try
    {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      Root<States> rt = cq.from(States.class);
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
