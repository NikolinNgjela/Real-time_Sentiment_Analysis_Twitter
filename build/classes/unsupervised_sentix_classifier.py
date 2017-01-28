# -*- coding: utf-8 -*-
from misc_filters1 import *
from pattern.it import *
import math

class SentixClassifier:    
    def __init__(self,sentiwordcsvname,amplifiersfilename,decrementersfilename):
        self.sentiworddic = importsentixcomplex(sentiwordcsvname)
        self.amplifiers = loadlines(amplifiersfilename)
        self.decrementers = loadlines(decrementersfilename)
        self.gammaampfunc = self.gamma_amp0
        self.wsize = 5
        self.gammanegfunc = self.gamma_neg0
    
    # basic amplifier: returns 2 only on the next word, 0 otherwise
    def gamma_amp0(self,i):
        if i == 0:
            return 0
        if i < -math.ceil(self.wsize/2)+1 or i > math.floor(self.wsize/2):
            return 0
        if i == 1:
            return 2
        else:
            return 0
    # basic decrementer: returns -1 only on the next word, 0 otherwise
    def gamma_neg0(self,i):
        if i == 0:
            return 0
        if i < -math.ceil(self.wsize/2)+1 or i > math.floor(self.wsize/2):
            return 0
        if i == 1:
            return -1
        else:
            return 0
    
    def outfun(self,score):
        if type(score) is not complex:
            return 45.0 # pi/4 radians == 45 degrees
        return math.atan2(score.imag,score.real)*180/math.pi
    
    def is_definite_form(self,textlist,i):
        # input index should already have been tagged as VB or VBN
        # infinitive
        if lemma(textlist[i][0]) == textlist[i][0]:
            return -1
        # gerund
        if conjugate(textlist[i][0],GERUND) == textlist[i][0]:
            return -1
        # use 'VBN' tag, better
        if textlist[i][1] == 'VBN':
            return -1
        # past participle (without auxiliary verb)
        # implicit: tag(textlist[i]) == VB or VBN
        #if i-1 >= 0:
            #ptag = tag(textlist[i-1])
            #if ptag[0][1] == 'VB' and lemma(textlist[i-1]) != 'avere' and lemma(textlist[i-1]) != 'essere':
                #return 0
        return 1
    
    def polarity_by_tenses(self,text):
        # POS tagging
        tt=tag(text)
        # indefinite forms lowen polarity, definite ones raise it
        verbidxs = []
        for i in list(range(len(tt))):
            if tt[i][1] == 'VB' or tt[i][1] == 'VBN':
                verbidxs.append(i)
        # accumulate polarity scores
        pscore = 0
        for i in verbidxs:
            pscore += self.is_definite_form(tt,i)
        return pscore
    
    # not to be used, assumes that every text has already been cleaned
    def polarity_by_tenses_batch(self,texts):
        r=[]
        for i in texts:
            r.append(self.polarity_by_tenses(i))
        return r
    
    def cleanandscore_sentiwords_singletext(self,text):
        # remove mentions and hastags, and URLs
        t0 = tco00.sub(' ',htg.sub(' ',mnt.sub(' ',text.lower())))
        t1 = lowlevelclean(re.sub('[\r\n\t]',' ',utf16emojis2.sub(' ',utf16emojis1.sub(' ',t0))))
        
        score=complex(0)
        for i in t1.split():
            if i in self.sentiworddic.keys():
                score = score + self.sentiworddic[i]
        a=abs(score)
        if a != 0:
            return self.outfun(score/a)
        else:
            return 0
    
    def cleanandscore_with_modifiers(self,text):
        # remove mentions and hastags, and URLs
        t0 = tco00.sub(' ',htg.sub(' ',mnt.sub(' ',text.lower())))
        t1 = lowlevelclean(re.sub('[\r\n\t]',' ',utf16emojis2.sub(' ',utf16emojis1.sub(' ',t0))))
        l=t1.split()
        lmax=len(l)
        
        # array of modifier coefficients
        mcoeffs = []
        for w in l:
            mcoeffs.append(1)
        
        for i in list(range(0,lmax)):
            if l[i] in self.amplifiers:
                for j in list(range(1,int(math.floor(self.wsize/2)+1))):
                    if i+j < lmax:
                        mcoeffs[i+j] = mcoeffs[i+j] + self.gammaampfunc(j)
            if l[i] in self.decrementers:
                for j in list(range(1,int(math.floor(self.wsize/2)+1))):
                    if i+j < lmax:
                        mcoeffs[i+j] = mcoeffs[i+j] + self.gammanegfunc(j)
        
        # array of sentiment scores
        sentscores=[]
        for w in l:
            if w in self.sentiworddic.keys():
                sentscores.append(self.sentiworddic[w])
            else:
                sentscores.append(0)
        
        # dot product
        score = complex(0)
        for i in list(range(0,lmax)):
            score = score + mcoeffs[i]*sentscores[i]
        
        a=abs(score)
        if a != 0:
            return self.outfun(score/a)
        else:
            return 45.0 # pi/4 radians == 45 degrees
    
    def cleanandscore_with_modifiers_batch(self,texts):
        r=[]
        for i in texts:
            r.append(self.cleanandscore_with_modifiers(i))
        return r
    
    # sentiment + subjectivity
    def cleanandscore_full(self,texts):
        r=[] # sentiment scores
        sj=[] # subjectivity scores
        for text in texts:
            # remove mentions and hastags, and URLs
            t0 = tco00.sub(' ',htg.sub(' ',mnt.sub(' ',text.lower())))
            t1 = lowlevelclean(re.sub('[\r\n\t]',' ',utf16emojis2.sub(' ',utf16emojis1.sub(' ',t0))))
            l=t1.split()
            lmax=len(l)
            
            ### subjectivity ###
            
            # POS tagging
            tt=tag(t1)
            # indefinite forms lowen polarity, definite ones raise it
            verbidxs = []
            for i in list(range(len(tt))):
                if tt[i][1] == 'VB' or tt[i][1] == 'VBN':
                    verbidxs.append(i)
            # accumulate polarity scores
            pscore = 0
            for i in verbidxs:
                pscore += self.is_definite_form(tt,i)
            sj.append(pscore)
            
            ### sentiment ###
            
            # array of modifier coefficients
            mcoeffs = []
            for w in l:
                mcoeffs.append(1)
            
            for i in list(range(0,lmax)):
                if l[i] in self.amplifiers:
                    for j in list(range(1,int(math.floor(self.wsize/2)+1))):
                        if i+j < lmax:
                            mcoeffs[i+j] = mcoeffs[i+j] + self.gammaampfunc(j)
                if l[i] in self.decrementers:
                    for j in list(range(1,int(math.floor(self.wsize/2)+1))):
                        if i+j < lmax:
                            mcoeffs[i+j] = mcoeffs[i+j] + self.gammanegfunc(j)
            
            # array of sentiment scores
            sentscores=[]
            for w in l:
                if w in self.sentiworddic.keys():
                    sentscores.append(self.sentiworddic[w])
                else:
                    sentscores.append(0)
            
            # dot product
            score = complex(0)
            for i in list(range(0,lmax)):
                score = score + mcoeffs[i]*sentscores[i]
            
            a=abs(score)
            if a != 0:
                b = self.outfun(score/a)
            else:
                b = 45.0
            r.append(b)
        return (r,sj)
