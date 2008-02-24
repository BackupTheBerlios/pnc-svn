package com.mathias.bellatetris.server;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

public class HighscoreItemDao {

	private SessionFactory sf;
	
	public HighscoreItemDao(){
		try{
			sf = new AnnotationConfiguration().addAnnotatedClass(
					HighscoreItem.class).configure().buildSessionFactory();
		}catch(Throwable e){
			System.err.println("Initial SessionFactory creation failed: " + e);
		}
	}

	private Session getSession(){
		Session s = sf.getCurrentSession();
		if(s == null){
			s = sf.openSession();
		}
		return s;
	}

	public List<HighscoreItem> getHighscores(){
		List<HighscoreItem> list;
		Transaction tx = getSession().beginTransaction();
		list = getSession().createCriteria(HighscoreItem.class).list();
		tx.commit();
		return list;
	}

	public void saveHighscore(HighscoreItem item){
		Transaction tx = getSession().beginTransaction();
		getSession().save(item);
		tx.commit();
	}

}
