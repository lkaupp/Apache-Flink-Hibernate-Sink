import com.isra.LogEvent_TimeMessage;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import com.isra.TimeAndMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by CloudResearch on 13.03.2017.
 */
public class HibernateSink extends RichSinkFunction<TimeAndMessage> {
    private static final SessionFactory ourSessionFactory;
    private static final ServiceRegistry serviceRegistry;
    private List<TimeAndMessage> queue;
    private List<TimeAndMessage> synchronizedCopy;

    private Session session;
    static {
        try {
            org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
            configuration.addAnnotatedClass(HibernateTimeAndMessage.class);
            configuration.configure();

            serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            ourSessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public void invoke(TimeAndMessage timeAndMessage) throws Exception {

        queue.add(timeAndMessage);

       if(queue.size() == 20) {
           synchronized (synchronizedCopy) {
               synchronizedCopy = new ArrayList<TimeAndMessage>(queue);
               queue.clear();
               Iterator<TimeAndMessage> it = synchronizedCopy.iterator();
               Transaction transaction = null;

               while (it.hasNext()) {
                   try {
                       transaction = session.beginTransaction();

                       int numRecordsProcessed = 0;

                       while (numRecordsProcessed < 20 && it.hasNext()) {
                           TimeAndMessage timeAndMessage2 = it.next();
                           Date date = new Date(timeAndMessage2.getTimestamp().getTime());
                           HibernateTimeAndMessage htam = new HibernateTimeAndMessage();
                           htam.setDate(date);
                           htam.setMessage(timeAndMessage2.getMessage());
                           htam.setPath(timeAndMessage2.getPath());
                           session.save(htam);

                           numRecordsProcessed++;
                       }
                       session.flush();
                   } catch (HibernateException e) {
                       throw new HibernateException("Cannot save member", e);
                   } finally {
                       transaction.commit();
                   }
               }
           }
       }


    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.session = ourSessionFactory.openSession();
        this.queue = new ArrayList<>();
        this.synchronizedCopy = new ArrayList<>();
    }

    @Override
    public void close() throws Exception {
       this.session.close();
    }
}
