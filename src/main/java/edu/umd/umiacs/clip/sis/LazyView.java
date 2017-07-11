package edu.umd.umiacs.clip.sis;

/**
 *
 * @author Mossaab Bagdouri
 */
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.component.selectonebutton.SelectOneButton;
import org.primefaces.event.SelectEvent;

@ManagedBean
@ViewScoped
public class LazyView implements Serializable {

    private LazyEmailDataModel lazyModel;
    private String query = "";

    private Email selectedEmail;

    @ManagedProperty("#{applicationBean}")
    private transient ApplicationBean applicationBean;

    @PostConstruct
    public void init() {
        lazyModel = new LazyEmailDataModel(applicationBean.getIs(), applicationBean.getAnnotations(), query);
        selectedEmail = lazyModel.getFirstEmail();
    }

    public LazyEmailDataModel getLazyModel() {
        return lazyModel;
    }

    public Email getSelectedEmail() {
        if (selectedEmail == null) {
            selectedEmail = lazyModel.getFirstEmail();
        }
        return selectedEmail;
    }

    public void setSelectedEmail(Email selectedEmail) {
        this.selectedEmail = selectedEmail;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void onRowSelect(SelectEvent event) {
        selectedEmail = ((Email) event.getObject());
    }

    public void submit() {
        lazyModel = new LazyEmailDataModel(applicationBean.getIs(), applicationBean.getAnnotations(), query);
        selectedEmail = lazyModel.getFirstEmail();
    }

    public void filter(String value) {
        lazyModel = new LazyEmailDataModel(applicationBean.getIs(), applicationBean.getAnnotations(), value);
        selectedEmail = lazyModel.getFirstEmail();
    }

    public void searchEmail(String left, String right) {
        lazyModel = new LazyEmailDataModel(applicationBean.getIs(), applicationBean.getAnnotations(), "\"" + left.replace("\"", " ") + "\" \"" + right.replace("\"", " ") + "\"");
        selectedEmail = lazyModel.getFirstEmail();
    }

    public void predict() {
        lazyModel = new LazyEmailDataModel(applicationBean.getIs(), applicationBean.getAnnotations(), applicationBean.getPredictions(), false);
        selectedEmail = lazyModel.getFirstEmail();
    }

    public void activeLearning() {
        float[] predictions;
        if (!applicationBean.getAnnotations().isEmpty()) {
            applicationBean.train();
            applicationBean.predict();
            predictions = applicationBean.getPredictions();
        } else {
            predictions = new float[applicationBean.getIs().getIndexReader().numDocs()];
        }
        lazyModel = new LazyEmailDataModel(applicationBean.getIs(), applicationBean.getAnnotations(), predictions, true);
        selectedEmail = lazyModel.getFirstEmail();
    }

    public void updateAnnotations(Email email) {
        this.selectedEmail = email;
        String message;
        if (email.getAnnotation() == 0) {
            applicationBean.getAnnotations().remove(email.getId());
            message = "Annotation removed.";
        } else {
            applicationBean.getAnnotations().put(email.getId(), email.getAnnotation() == 1);
            message = "Email " + (applicationBean.getAnnotations().get(email.getId()) ? "protected" : "released") + ".";
        }
        applicationBean.saveAnnotations();
        FacesMessage msg = new FacesMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void subjectSelectionChanged(AjaxBehaviorEvent event) {
        System.out.println(event.getClass().getSimpleName());
        System.out.println(event.getSource());
        SelectOneButton source = (SelectOneButton) event.getSource();
        System.out.println(source.getValue());
        System.out.println(source.getLocalValue());
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }
}
