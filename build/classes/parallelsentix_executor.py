# -*- coding: utf-8 -*-
from misc_filters1 import *
from pattern.it import *
import math
from cpucount import *
import threading
import time
import pickle
from java.util.concurrent import Callable, Executors, TimeUnit
# from shutdown import shutdown_and_await_termination 

### WARNING: only for Jython (does not work on CPython)

def dumpdictionariestofile(sentiworddic,amplifiers,decrementers,filename):
	dictionaries = (sentiworddic,amplifiers,decrementers)
	f = open(filename,'wb')
	pickle.dump(dictionaries,f)
	f.close()

def loaddictionariesfromfile(filename):
	f = open(filename,'rb')
	dictionaries = pickle.load(f)
	f.close()
	return dictionaries

class SentixClassifier:
	# default constructor: init from dictionary objects
	def __init__(self, sentiworddic, amplifiers, decrementers):
		self.sentiworddic = sentiworddic
		self.amplifiers = amplifiers
		self.decrementers = decrementers
		self.gammaampfunc = self.gamma_amp0
		self.wsize = 5
		self.gammanegfunc = self.gamma_neg0
	
	#alternate constructor: init from dictionary files
	@classmethod
	def initfromfiles(cls, sentiwordcsvname, amplifiersfilename, decrementersfilename):
		sentiworddic = importsentixcomplex(sentiwordcsvname)
		amplifiers = loadlines(amplifiersfilename)
		decrementers = loadlines(decrementersfilename)
		return cls(sentiworddic,amplifiers,decrementers)
	#############################
		
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
	
	def outfun(self,score): # saturation function for sentiment classes
		if type(score) is not complex:
			return 0
		angle = math.atan2(score.imag,score.real)*180/math.pi
		if angle < 36.0:
			return 1 # positive
		elif angle < 54.0:
			return 0 # neutral
		else:
			return -1
	
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
			return 0
	
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
				b = 0
			r.append(b)
		return (r,sj)

class Worker(Callable):
	# Classification type: 0 = sentiment only; 1 = sentiment + subjectivity
	def __init__(self, sentiworddic, amplifiers, decrementers, tid, tdata, classificationtype=0):
		self.classificationtype = classificationtype
		self.started = None
		self.completed = None
		self.result = None
		self.thread_used = None
		self.exception = None
		self.tid = tid # thread ID
		self.tdata = tdata # texts chunk (list of texts to be cleaned and classified)
		self.S=SentixClassifier(sentiworddic, amplifiers, decrementers)
		
	def __str__(self):
		if self.exception:
			return "error in thread n. %s" % self.tid
		elif self.completed:
			return "completed thread n. %s" % self.tid
		elif self.started:
			return "started thread n. %s" % self.tid
		else:
			return "thread n. %s not yet scheduled" % self.tid
	
	def call(self):
		self.thread_used = threading.currentThread().getName()
		self.started = time.time()
		try:
			if self.classificationtype == 0:
				self.result = self.S.cleanandscore_with_modifiers_batch(self.tdata)
			else:
				self.result = self.S.cleanandscore_full(self.tdata)
		except Exception, ex:
			self.exception = ex
		self.completed = time.time()
		return self

class MasterCaller:
	def __init__(self, sentiworddicname = None, amplifiersname = None, decrementersname = None, serializedDictsName = None):
		if serializedDictsName is not None:
			(self.sentiworddic,self.amplifiers,self.decrementers) = loaddictionariesfromfile(serializedDictsName)
		else:
			self.sentiworddic = importsentixcomplex(sentiworddicname)
			self.amplifiers = loadlines(amplifiersname)
			self.decrementers = loadlines(decrementersname)
		# start thread pool
		self.k = available_cpu_count()
		self.pool = Executors.newFixedThreadPool(self.k)
		
	def classify_sent(self,texts):
		self.resd = {} # dictionary for sorting return data from threads
		self.results = []
		self.n = len(texts)
		
		# optimal static splitting
		self.tdatas = []
		for i in range(self.k):
			self.tdatas.append(texts[(self.n*i)/self.k : (self.n*(i+1))/self.k])
		
		# create and start threads
		self.workers = [Worker(self.sentiworddic,self.amplifiers,self.decrementers,i,self.tdatas[i],classificationtype=0) for i in range(self.k)]
		self.futures = self.pool.invokeAll(self.workers)
	
		for future in self.futures:
			f = future.get(5, TimeUnit.SECONDS)
			self.resd[f.tid] = f.result
		
		# sort results
		for i in range(self.k):
			self.results += self.resd[i]
		
		return self.results
	
	def classify_full(self,texts):
		self.resd = {} # dictionary for sorting return data from threads
		self.resultsents = []
		self.resultsubjs = []
		self.n = len(texts)
		
		# optimal static splitting
		self.tdatas = []
		for i in range(self.k):
			self.tdatas.append(texts[(self.n*i)/self.k : (self.n*(i+1))/self.k])
		
		# create and start threads
		self.workers = [Worker(self.sentiworddic,self.amplifiers,self.decrementers,i,self.tdatas[i],classificationtype=1) for i in range(self.k)]
		self.futures = self.pool.invokeAll(self.workers)
	
		for future in self.futures:
			f = future.get(5, TimeUnit.SECONDS)
			self.resd[f.tid] = f.result
		
		# sort results
		for i in range(self.k):
			self.resultsents += self.resd[i][0]
			self.resultsubjs += self.resd[i][1]
		
		return (self.resultsents,self.resultsubjs)


if __name__ == "__main__":
	# read dictionaries
	sentiworddic = importsentixcomplex('sentix3.csv')
	amplifiers = loadlines('amplifiers.txt')
	decrementers = loadlines('decreasers.txt')
	
	resd = {}
	
	# read full input data
	f = open('texts.csv','r')
	cr = csv.reader(f)
	texts = []
	for r in cr:
		if len(r) > 0:
			texts.append(r[0])
	n = len(texts)
	
	# get CPU count
	k = available_cpu_count()
	tdatas = []
	
	# split the work (optimal static splitting)
	# implicit assumption: atomic units of work of equal weight (not true in this case)
	for i in range(k):
		tdatas.append(texts[(n*i)/k : (n*(i+1))/k])
	
	#######################################
	
	pool = Executors.newFixedThreadPool(k)
	workers = [Worker(sentiworddic,amplifiers,decrementers,i,tdatas[i]) for i in range(k)]
	futures = pool.invokeAll(workers)
	
	for future in futures:
		f = future.get(5, TimeUnit.SECONDS)
		resd[f.tid] = f.result
	
	# shutdown_and_await_termination(pool, 5)
