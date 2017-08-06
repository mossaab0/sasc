/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umd.umiacs.clip.sis;

/**
 *
 * @author Mossaab
 */
public class Label {
    public static final int PROTECT_SVM = 1;
    public static final int RELEASE_SVM = -1;
    public static final int DEFER_SVM = 0;
    public static final int NO_ANNOTATION_PF = 0;
    public static final int PROTECT_PF = 1;
    public static final int DEFER_PF = 2;
    public static final int RELEASE_PF = 3;
    public static final int toSVM(int pf) {
        switch (pf) {
            case PROTECT_PF:
                return PROTECT_SVM;
            case RELEASE_PF:
                return RELEASE_SVM;
            default:
                return DEFER_SVM;
        }
    }
    public static final int toPF(int svm) {
        switch (svm) {
            case PROTECT_SVM:
                return PROTECT_PF;
            case RELEASE_SVM:
                return RELEASE_PF;
            default:
                return DEFER_PF;
        }
    }
}
