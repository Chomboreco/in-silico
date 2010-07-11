/*
 * File:   main.cpp
 * Author: seb
 *
 * Created on 25 de junio de 2010, 02:59 PM
 */

#include <cstdlib>
#include <cstdio>
#include <cxtypes.h>
#include <highgui.h>
#include <cv.h>
#include "lineal.h"
//#include <cmath>

#define CAM 0
#define WIN "Ventana1"
#define MAXL 10
#define MINL 2

CvCapture* cap;
CvMemStorage *seq, *poly;
char fig; //figura del test case
char *fname;
bool mostrar=true;

void init_params() {
    if (mostrar) cvNamedWindow(WIN, CV_WINDOW_AUTOSIZE);
    cap = cvCreateCameraCapture(CAM);
    seq = cvCreateMemStorage();
    poly = cvCreateMemStorage();
}

void destroy_params() {
    cvReleaseCapture(&cap);
    //cvDestroyWindow(WIN);
    cvReleaseMemStorage(&seq);
    cvReleaseMemStorage(&poly);
}

IplImage* captureAndFilter() {
    IplImage* frame = 0;
    if (fig == '\0')
        frame = cvQueryFrame(cap);
    else
        frame = cvLoadImage( fname );
    if (mostrar) {
        cvShowImage(WIN, frame);
        cvWaitKey();
    }
    IplImage *img = cvCreateImage(cvSize(frame->width,frame->height),IPL_DEPTH_8U,1);
    cvCvtColor(frame,img,CV_RGB2GRAY);
    cvSmooth(img,img);
    cvAdaptiveThreshold(img, img, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY,15);
    cvDilate(img,img);
    cvErode(img,img);
    if (mostrar) {
        cvShowImage(WIN, img);
        cvWaitKey();
    }
    return img;
}

double mean(double *data, int n) {
    double u=0;
    for (int i=0; i<n; i++) {
        u += data[i];
    }
    return u/n;
}

double desv_est(double * data, double media, int n) {
    double u=0, tmp;
    for (int i=0; i<n; i++) {
        tmp = (data[i]-media);
        u += tmp*tmp;
    }
    return sqrt(u/n);
}

void printFeatures(CvSeq* poly) {
    int nlados = poly->total;
    if (nlados >= MAXL || nlados <= MINL) return;
    
    double lados[MAXL];
    double ang[MAXL];
    double points[MAXL][DIMS];
    double vects[MAXL][DIMS];
    for (int j=0; j<nlados; j++) {
        CvPoint* p = CV_GET_SEQ_ELEM( CvPoint, poly, j );
        points[j][0] = p->x;
        points[j][1] = p->y;
    }
    for (int j=0; j<nlados; j++) {
        diff(vects[j], points[j], points[(j+1) % nlados]);
        lados[j] = norm(vects[j]);
    }
    for (int j=0; j<nlados; j++) {
        ang[j] = vect_ang(vects[j],vects[(j+1)%nlados]);
    }
    double m_lados,m_ang,d_lados,d_ang;
    m_lados=mean(lados,nlados); m_ang=mean(ang,nlados);
    d_lados=desv_est(lados,m_lados,nlados); d_ang=desv_est(ang,m_ang,nlados);
    printf("%c %i %lf %lf\n",fig,nlados,d_lados/m_lados,d_ang/m_ang);
}

void getFeatures() {
    IplImage *img = captureAndFilter();
    cvClearMemStorage(seq);
    CvSeq* cont = 0, *p2=0;
    int Nc = cvFindContours(img,seq,&cont,sizeof(CvContour),CV_RETR_LIST);
    //p2 = cvApproxPoly(cont,sizeof(CvContour),poly,CV_POLY_APPROX_DP,20);
    if ( cont ) {
        for (CvSeq *i=cont; i!=NULL; i=i->h_next) {
            cvZero(img);
            p2 = cvApproxPoly(i,sizeof(CvContour),poly,CV_POLY_APPROX_DP,10);
            if (mostrar) {
                cvDrawContours(img,p2,cvScalarAll(255),cvScalarAll(255),0);
                cvShowImage(WIN,img);
                cvWaitKey();
            }
            printFeatures(p2);
        }
    }
    //printf("El número de contornos es: %i\n",Nc);
}

/*
 *
 */
int main(int argc, char** argv) {
    //destroy_params();
    init_params();
    argc--; argv++;
    if (argc > 0) {
        fig = argv[0][0];
        fname = argv[1];
        getFeatures();        
    }

    destroy_params();
    return (EXIT_SUCCESS);
}
