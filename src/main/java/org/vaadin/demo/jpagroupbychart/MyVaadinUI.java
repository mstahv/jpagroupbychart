package org.vaadin.demo.jpagroupbychart;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.PlotOptionsArea;
import com.vaadin.addon.charts.model.Stacking;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MyVaadinUI extends UI {

    private static final String PERSISTENCE_UNIT_NAME = "org.example";

    private static EntityManagerFactory factory;
    private static final String[] pages = new String[] { "index.html",
            "download.html", "join.html" };

    static {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

        EntityManager m = factory.createEntityManager();

        m.getTransaction().begin();
        Random random = new Random(0);
        for (int i = 0; i < 200; i++) {
            // Add some test data
            Hit hit = new Hit();
            Calendar cal = cal();
            cal.setLenient(true);
            cal.roll(Calendar.DAY_OF_YEAR, -random.nextInt(7*8));
            hit.setHitDate(cal.getTime());
            hit.setPage(pages[random.nextInt(pages.length)]);
            m.persist(hit);
        }

        m.getTransaction().commit();
        m.close();
    }

    private static Calendar cal() {
        Calendar cal = Calendar.getInstance();
        cal.set(2013, 3-1, 12);
        return cal;
    }

    private EntityManager em;
    private Chart chart;

    private VerticalLayout layout;

    public MyVaadinUI() {
        em = factory.createEntityManager();
    }

    @Override
    public void detach() {
        super.detach();
        em.close();
    }

    @Override
    protected void init(VaadinRequest request) {
         layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);
        final JPAContainer<Hit> container = JPAContainerFactory.make(Hit.class,
                em);
        final Table table = new Table("Weekly visits", container);
        table.setEditable(true);

        Button button = new Button("Add hit");
        button.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                Object id = container.addItem();
                table.setCurrentPageFirstItemId(id);
            }
        });
        layout.addComponent(button);

        button = new Button("Visualize");
        button.addClickListener(new ClickListener() {


            @Override
            public void buttonClick(ClickEvent event) {
                
                if(chart != null) {
                    layout.removeComponent(chart);
                }

                chart = new Chart(ChartType.AREA);
                
                PlotOptionsArea plotOptionsArea = new PlotOptionsArea();
                plotOptionsArea.setStacking(Stacking.NORMAL);
                chart.getConfiguration().setPlotOptions(plotOptionsArea);
                
                chart.getConfiguration().setTitle("Page hits");
                chart.getConfiguration().getyAxis().setTitle("Hits");
                chart.getConfiguration().getxAxis().setTitle("Week Nr");
                

                for (String p : pages) {
                    Query q = em
                            .createQuery("SELECT FUNC('WEEK', h.hitDate) AS week, COUNT(h) FROM Hit h WHERE h.page = :page GROUP BY week ORDER BY week");
                    q.setParameter("page", p);
                    List<Object[]> resultList = q.getResultList();
                    DataSeries ds = new DataSeries();
                    ds.setName(p);
                    for (Object[] r : resultList) {
                        Number week = (Number) r[0];
                        Number y = (Number) r[1];
                        ds.add(new DataSeriesItem(week, y));
                    }
                    chart.getConfiguration().addSeries(ds);
                }
                layout.addComponentAsFirst(chart);
            }
        });

        layout.addComponent(button);
        
        layout.addComponent(table);

    }
}
