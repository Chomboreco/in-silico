
#include "polynom.h"

Polynom::Polynom(int grado, int max) {
    if (max != 0)   this->coef = new double[max];
    this->max=max;
    clear();
    this->grado=grado;  
}

Polynom::~Polynom() {
    delete [] coef;
}

Polynom* Polynom::operator +=(Polynom* b) {
    int i = (b->grado > grado) ? b->grado : grado;
    this->grado=i;
    for (; i>=0; i--) {
        coef[i] += b->coef[i];
    }
    simplify();
    return this;
}

Polynom* Polynom::operator -=(Polynom* b) {
    int i = (b->grado > grado) ? b->grado : grado;
    this->grado=i;
    for (; i>=0; i--) {
        coef[i] -= b->coef[i];
    }
    simplify();
    return this;
}

Polynom* Polynom::operator *=(Polynom* b) {
    double nc[max];
    for (int i=0; i<max; i++) nc[i]=0.0;
    for (int i=0; i<=grado; i++) {
        for (int j=0; j<=b->grado; j++) {
            nc[i+j] += coef[i]*b->coef[j];
        }
    }
    for (int i=0; i<max; i++) coef[i]=nc[i];
    grado+=b->grado;
    simplify();
    return this;
}

Polynom* Polynom::operator /=(Polynom* b) {
    this->div(b, this, NULL);
    return this;
}

Polynom* Polynom::operator %=(Polynom* b) {
    Polynom* cos = new Polynom(0,max);
    Polynom* res = new Polynom(0,max);
    div(b,cos,res);
    this->copyFrom(res);
    delete cos; delete res;
    return this;
}

complex<double> Polynom::eval(complex<double> x) {
    complex<double> fx;
    for (int i=grado; i>=0; i--) {
        fx *= x;
        fx += coef[i];
    }
    return fx;
}

void Polynom::derivate() {
    for (int i=1; i<=grado; i++) {
        coef[i-1] = coef[i]*i;
    }
    grado--;
}

void Polynom::clear() {
    for (int i=0; i<max; i++) {
        coef[i]=0.0;
    }
    this->grado=0;
}

void Polynom::print() {
    for (int i=0; i<=grado; i++) {
        printf("%fx%i ",coef[i],i);
    }
    printf("\n");
}

void Polynom::simplify() {
    while (grado>0 && coef[grado] == 0) grado--;
}

void Polynom::copyFrom(Polynom* p) {
    this->grado = p->grado;
    for (int i=grado; i>=0; i--) {
        this->coef[i] = p->coef[i];
    }
}

void Polynom::remMultRoot() {
    Polynom *a = this;
    Polynom b(0,max);
    Polynom gcd(0,max);
    b.copyFrom(this);
    b.derivate();
    mcd(a,&b,&gcd);
    if (gcd.grado>0) *a /= &gcd;
}

//It doesn't change b or this. Just changes cos and return residuo if not null
void Polynom::div(Polynom* b, Polynom* cos, Polynom* residuo) {
    if (cos != this) cos->copyFrom(this);
    double *res = cos->coef;
    int n=grado, m=b->grado, ng=n-m;
    cos->coef = new double[max];
    clear();
    while (n>=m) {
        double val = res[n]/b->coef[m];
        cos->coef[n-m] = val;
        for (int i=m, j=n; i>=0; i--) {
            res[j--] -= val*b->coef[i];
        }
        n--;
    }
    cos->grado = ng;
    cos->simplify();
    if (residuo == NULL) delete [] res;
    else {
        delete [] residuo->coef;
        residuo->coef = res;
        residuo->max = cos->max;
        residuo->grado = b->grado;
        residuo->simplify();
    }
}

void Polynom::laguerre(complex<double>* roots) {
    Polynom poly(0,max); //the polynom
    Polynom polyp(0,max); //It's derivative
    Polynom polypp(0,max); //2nd order derivative
    poly.copyFrom(this);
    complex<double>* root = roots;
    complex<double> G,H,a,den,fx;
    complex<double> n(grado,0), one(1,0);
    while ( poly.grado>0 ) {
        polyp.copyFrom(&poly);
        polyp.derivate();
        polypp.copyFrom(&polyp);
        polypp.derivate();
        do {
            fx = poly.eval(*root);
            if (abs(fx) < ERR) break;
            G = polyp.eval(*root) / fx;
            H = G*G - polypp.eval(*root)/fx;
            den = sqrt( (n-one)*(H*n-G*G) );
            if (abs(G-den) > abs(G+den))
                den = G-den;
            else
                den = G+den;
            a = n / den;
            *root -= a;
        } while (abs(a)>PREC);
        if (root->imag() < ERR) {
            polyp.grado=1;
            polyp.coef[0]=-1*root->real();
            polyp.coef[1]=1;
            poly /= &polyp;           
            root++;
        } else {
            polyp.grado=2;
            polyp.coef[0]=norm(*root);
            polyp.coef[1]=-2*root->real();
            polyp.coef[2]=1;
            poly /= &polyp;
            root[1] = conj(*root);
            root+=2;
        }
    }
}

bool Polynom::isZero() {
    return ( this->grado==0 && *(this->coef)==0.0 );
}

void mcd(Polynom* na, Polynom* nb, Polynom *res) {
    Polynom a(0,20); Polynom b(0,20);
    a.copyFrom(na); b.copyFrom(nb);
    Polynom *t = new Polynom(0,a.max);
    Polynom *residuo = new Polynom(0,a.max);
    while (! b.isZero() ) {
        a.div(&b,t,residuo);
        a.copyFrom(&b);
        b.copyFrom(residuo);
        t->clear();
    }
    delete t; delete residuo;
    res->copyFrom(&a);
}