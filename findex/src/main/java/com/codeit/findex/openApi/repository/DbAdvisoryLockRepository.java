package com.codeit.findex.openApi.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class DbAdvisoryLockRepository {

	@PersistenceContext
	private EntityManager em;

	@Transactional(propagation = Propagation.MANDATORY, readOnly = true)
	public boolean tryAdvisoryXactLock(long lockKey) {
		Boolean ok = (Boolean) em.createNativeQuery("SELECT pg_try_advisory_xact_lock(:k)")
			.setParameter("k", lockKey)
			.getSingleResult();
		return Boolean.TRUE.equals(ok);
	}

}
