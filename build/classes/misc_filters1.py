# -*- coding: utf-8 -*-
import sys
import re
import csv
import json
import codecs
import gc

endlineellipsis = re.compile(ur' [^ ]*\\u2026\n')
repeatedlinefeeds = re.compile(ur'\n(\n)+')
htmlents = re.compile(ur'&[^\s]*;')
escapedutf8repls_dic={u"\\u00e0":u"à",u"\\u00e8":u"è",u"\\u00e9":u"é",u"\\u00ec":u"ì",u"\\u00f2":u"ò",u"\\u00f9":u"ù"}
escapedutf8repls = re.compile('|'.join(re.escape(key) for key in escapedutf8repls_dic.keys()))
escapedunicode=re.compile(ur'\\u[a-f0-9][a-f0-9][a-f0-9][a-f0-9]')
escapedlinefeed=re.compile(ur'\\n')
italianchars=re.compile(ur'[^a-zA-Z0-9àèéìòù \t]')
utf16emojis1=re.compile(ur'\\ud83d\\u[a-f0-9][a-f0-9][a-f0-9][a-f0-9]')
utf16emojis2=re.compile(ur'\\ud83c\\u[a-f0-9][a-f0-9][a-f0-9][a-f0-9]')
repspaces=re.compile(ur' +')
# strip retweets, hashtags and mentions
rtw = re.compile(ur'^.*RT .*$', re.MULTILINE)
rtw1= re.compile(ur'\bRT\b')
mnt = re.compile(ur'@([^ \.,:;\r\n,]+)')
htg = re.compile(ur'#([^ \.,:;\r\n,]+)')
tco = re.compile(ur'https?:\\\/\\\/t\.co\\\/[a-zA-Z0-9]+') # for JSON input
tco00 = re.compile(ur'(https?:\\\/\\\/)?t\.co(\\\/[a-zA-Z0-9]+)?')
tco0 = re.compile(ur'https?:\/\/t\.co\/[a-zA-Z0-9]+') # for standard input

def importsentixscoredict(csvinname):
    f = codecs.open(csvinname, 'r','utf-8')
    r = csv.reader(f, delimiter=';')
    d = {}
    for rows in r:
        if "_" not in rows[0]:
            if rows[0] in d.keys():
                k=rows[0]
                l = d[rows[0]]
                l.append(int(rows[1]))
                d[k]=l
            else:
                d[rows[0]]=[int(rows[1])]
    f.close()

    for i in d.keys():
        avg=sum(d[i]) / float(len(d[i]))
        d.update({i:avg})
    return d

def unicodecsvreader(utf8_data,delimiter='\t',**kwargs):
    csv_reader = csv.reader(utf8_data,delimiter=delimiter,**kwargs)
    for row in csv_reader:
        yield [unicode(cell,'utf-8') for cell in row]

def importsentixcomplex(csvinname):
    f=open(csvinname)
    # f = codecs.open(csvinname, 'r','utf-8')
    # r = csv.reader(f, delimiter='\t')
    r = unicodecsvreader(f)
    d = {}
    for rows in r:
        if "_" not in rows[0]:
            k = rows[0].lower()
            if k in d.keys():
                l = d[k]
                l.append(complex(float(rows[1]),float(rows[2])))
                d[k]=l
            else:
                d[k]=[complex(float(rows[1]),float(rows[2]))]
    f.close()

    for i in d.keys():
        avg=sum(d[i])
        avg=avg/abs(avg)
        d.update({i:avg})
    return d

def importemoticonscoredict(csvinname):
  f=open(csvinname,'r')
  r=csv.reader(f,delimiter=';')
  d = {rows[0]:rows[1] for rows in r}
  f.close()
  return d

# low level cleaning
def lowlevelclean(s):
  t = escapedutf8repls.sub(lambda x: escapedutf8repls_dic[x.group()], s)
  t = htmlents.sub(' ',escapedunicode.sub(' ',escapedlinefeed.sub(' ',t)))
  return repspaces.sub(' ',italianchars.sub(' ',t))

# for training data
def cleanandscore(inname,outname,dicname):
  f = open(inname,'r')
  # explicit output encoding assignment is needed, otherwise Python will choose system's default encoding (Latin-1 for Windows)
  g = codecs.open(outname,'w',encoding='utf-8')
  gc = csv.writer(g,delimiter='\t')
  
  # preserve lines with UTF-16 emoticons
  cnt=[]
  for line in f.readlines():
    if utf16emojis1.search(line) is not None or utf16emojis2.search(line) is not None:
      cnt.append(line)
  f.close()
  # strip duplicate lines
  cnt1 = list(set(cnt))
  cnt = []
  # strip retweets, hashtags and mentions
  for line in cnt1:
    if rtw.search(line) is None:
      cnt.append(line)
  # remove mentions and hastags, and URLs
  cnt1 = cnt
  cnt = []
  for line in cnt1:
    cnt.append(tco.sub(' ',htg.sub(' ',mnt.sub(' ',line))))
  # append emoticon scores
  lines = cnt;
  cnt = None;
  dic=importemoticonscoredict(dicname)
  keys = dic.keys()
  for i in list(range(0,len(lines))):
    items = utf16emojis1.findall(lines[i]) + utf16emojis2.findall(lines[i])
    score=0
    # accumulate score for emojis in the score dictionary
    for em in items:
      if em in keys:
        score=score+int(dic[em])
    # remove all emojis in the non-BMP UTF-16 block and append score
    lines[i] = [re.sub('[\r\n\t]',' ',utf16emojis2.sub(' ',utf16emojis1.sub(' ',lines[i]))) , str(score)]
    # low-level cleaning and line feed restore
    lines[i][0] = lowlevelclean(lines[i][0])
  # write to csv file
  gc.writerows(lines)
  g.close()

def mergecolumns(file1, file2, outname):
  f = loadlines(file1)
  h = loadlines(file2)
  g = codecs.open(outname,'w',encoding='utf-8')
  gc = csv.writer(g,quoting=csv.QUOTE_NONE, escapechar=' ', quotechar=' ',delimiter='\t')
  lines = []
  # f and g should have the same number of lines
  for i in list(range(0,len(f))):
    lines.append([f[i],h[i]])
  gc.writerows(lines)
  g.close()

def satfunc3(i):
  if i<-0.5:
    return -1
  if i<0.5:
    return 0
  return 1

def cleanandscore_emoji_sentiwords(inname,outname,emdicname,sentiworddicname,saturationfunction):
  f = open(inname,'r')
  # explicit output encoding assignment is needed, otherwise Python will choose system's default encoding (Latin-1 for Windows)
  g = codecs.open(outname,'w',encoding='utf-8')
  gc = csv.writer(g,delimiter='\t')
  cnt=[]
  # preserve all lines (lines containing neither emoticons nor sentiwords will have zero score)
  for line in f.readlines():
    cnt.append(line.lower())
  f.close()
  # strip duplicate lines
  cnt1 = list(set(cnt))
  cnt = []
  # strip retweets, hashtags and mentions
  for line in cnt1:
    if rtw.search(line) is None:
      cnt.append(line)
  # remove mentions and hastags, and URLs
  cnt1 = cnt
  cnt = []
  for line in cnt1:
    cnt.append(tco.sub(' ',htg.sub(' ',mnt.sub(' ',line))))
  ### append emoticon scores ###
  lines = cnt;
  cnt = None;
  ed=importemoticonscoredict(emdicname) # contains integer scores
  sd=importsentixscoredict(sentiworddicname) # contains averaged floating point scores
  for i in list(range(0,len(lines))):
    items = utf16emojis1.findall(lines[i]) + utf16emojis2.findall(lines[i])
    score=0
    # accumulate score for emojis in the score dictionary
    for em in items:
      if em in ed.keys():
        score=score+int(ed[em])
    # remove all emojis in the non-BMP UTF-16 block and append score
    lines[i] = lowlevelclean(re.sub('[\r\n\t]',' ',utf16emojis2.sub(' ',utf16emojis1.sub(' ',lines[i]))))
    # after cleaning, add sentiword contributes to accumulated score
    items = lines[i].split()
    for w in items:
      if w in sd.keys():
        score=score+float(sd[w])
    ### saturation to be done here, or in the saturate method (or even better, set number of classes as input parameter) ###
    lines[i] = [lines[i], str(saturationfunction(score))]
  # write to csv file
  gc.writerows(lines)
  g.close()

# for input data (after training)
def cleantweet(s):
  cnt = rtw1.sub(' ',tco00.sub(' ',htg.sub(' ',mnt.sub(' ',endlineellipsis.sub('\n',s)))))
  cnt = re.sub('[\r\n\t]',' ',cnt)
  return lowlevelclean(cnt)

def cleantweetfile(inname,outname):
  f=codecs.open(inname,'r','utf-8')
  g=codecs.open(outname,'w','utf-8')
  l=[]
  for line in f:
    l.append(cleantweet(line)+'\n')
  g.writelines(l)

def timeindexed_cleaned_stopworded_tweets(inname,outname,swfilename,*args):
  f=codecs.open(inname,'r','utf-8')
  g=codecs.open(outname,'w','utf-8')
  ### strip duplicates ###
  s = set([])
  l = [] # texts in the original order
  idx = [] # time indices in the original order
  r=csv.reader(f,delimiter='\t')
  for rows in r:
    if rows[1] not in s: # check text duplicate
      s.add(rows[1])
      l.append((rows[0],rows[1]))
  # l is a list of tuples containing attributes for non-duplicate lines
  ### prefiltering ###
  cnt=[]
  rs=""
  for a in args:
    rs=rs+str(a)+"|"
  rs=rs[:-1]
  rsr=re.compile(rs,re.IGNORECASE)
  for t in l:
    if rsr.findall(t[1]) != []:
      cnt.append(t)
  # cnt contains tuples with text filtered upon input strings
  ### text cleaning ###
  l = []
  for t in cnt:
    l.append((t[0],cleantweet(t[1])))
  # l contains tuples with time index and pure text lines (not still stopworded)
  ### stopwording ###
  cnt=[]
  swl = loadlines(swfilename)
  swl = swl + list(args) # add the prefiltering words to the stopwords list
  for t in l:
    els = t[1].split(' ')
    filtered=[word for word in els if word.lower() not in swl and len(word) > 2]
    cnt.append(t[0]+'\t'+' '.join(filtered)+'\n')
  g.writelines(cnt)

def keeplineswithemoticonsascii(inname,outname):
    p = re.compile(r'(?::|;|=)(?:-)?(?:\)|\(|D|P)')
    f = open(inname,'r')
    g = open(outname,'w')
    cnt=[]
    for line in f.readlines():
        if p.search(line) is not None:
            cnt.append(line)
    f.close()
    g.writelines(cnt)
    g.close()

def saturatescorestobipartite(inname,outname):
        f=open(inname,'r')
        g=open(outname,'w',newline='')
        c = csv.reader(f, delimiter='\t')
        ls = [l for l in c]
        f.close()
        for i in list(range(0,len(ls))):
                if int(ls[i][1]) <= 0:
                        ls[i][1] = str(-1)
                else:
                        ls[i][1] = str(1)
        w = csv.writer(g,delimiter='\t')
        w.writerows(ls)
        g.close()

def saturate2(inname,outname,outname1):
        f=open(inname,'r')
        g=open(outname,'w',newline='')
        h=open(outname1,'w',newline='')
        c = csv.reader(f, delimiter='\t')
        ls = [l for l in c]
        l0 = []
        l1 = []
        f.close()
        for i in list(range(0,len(ls))):
                sent = int(ls[i][1])
                if sent < 0:
                        l1.append(ls[i][0]+'\n')
                elif sent > 0:
                        l0.append(ls[i][0]+'\n')
                else:
                        pass
        g.writelines(l0)
        h.writelines(l1)
        g.close()
        h.close()

def saturate3(inname,negname,neutname,posname):
        f=open(inname,'r')
        g=open(negname,'w',newline='')
        h=open(neutname,'w',newline='')
        j=open(posname,'w',newline='')
        c = csv.reader(f, delimiter='\t')
        ls = [l for l in c]
        l0 = []
        l1 = []
        l2 = []
        f.close()
        for i in list(range(0,len(ls))):
                sent = int(ls[i][1])
                if sent < 0:
                        l0.append(ls[i][0]+'\n')
                elif sent == 0:
                        l1.append(ls[i][0]+'\n')
                else:
                        l2.append(ls[i][0]+'\n')
        g.writelines(l0)
        h.writelines(l1)
        j.writelines(l2)
        g.close()
        h.close()
        j.close()

def prepareforweka(inname,outname):
        f=open(inname,'r')
        g=open(outname,'w',newline='')
        c = csv.reader(f, delimiter='\t')
        ls = [['textcontent','sentimentscore']]+[l for l in c]
        f.close()
        for i in list(range(1,len(ls))):
                ls[i][0] = "\"" +ls[i][0]+ "\"" 
        w = csv.writer(g,quoting=csv.QUOTE_NONE, escapechar=' ', quotechar=' ',delimiter='\t')
        w.writerows(ls)
        g.close()

def removeblanklines(inname,outname):
    f=codecs.open(inname,'r','utf-8')
    g=codecs.open(outname,'w','utf-8')
    cnt=f.read()
    f.close()
    # cnt=repeatedlinefeeds.sub('\n',cnt)
    cnt=re.sub(r'(\n)+','\n',cnt)
    g.write(cnt)
    g.close()

def prefilter(inname,outname,*args):
    f=codecs.open(inname,'r','utf-8')
    g=codecs.open(outname,'w','utf-8')
    cnt=[]
    rs=""
    print(args)
    for a in args:
        rs=rs+str(a)+"|"
    rs=rs[:-1]
    r=re.compile(rs,re.IGNORECASE)
    for line in f:
        if r.findall(line) != []:
            cnt.append(line)
    g.writelines(cnt)

#################################################
# strip duplicates in CSV tab-separated file with timestamp and text
def stripdups(inname,outname):
  f=open(inname,'r')
  g=open(outname,'w')
  s = set([])
  l = []
  r=csv.reader(f,delimiter='\t')
  for rows in r:
    if rows[1] not in s: # check text duplicate
      s.add(rows[1])
      l.append(rows[0]+'\t'+rows[1]+'\n')
  g.writelines(l)

def loadlines(filename):
    f=codecs.open(filename,'r','utf-8')
    rawcontent=f.read()
    f.close()
    return rawcontent.split('\n')
 
def removeSw(s,swList):
    wList=s.split(' ')
    filtered=[word for word in wList if word not in swList]
    return ' '.join(filtered)

def histogram(words):
    freq = {}
    for w in words:
        if w in freq:
            freq[w] = freq[w] + 1
        else:
            freq[w] = 1
    return freq

def top_words(d,limit):
    d_view = [ (v,k) for k,v in d.items() ]
    d_view.sort(reverse=True) # natively sort tuples by first element
    return d_view[0:limit]
#    for v,k in d_view[0:limit]:
#        print("%s: %d" % (k,v))
#################################################
