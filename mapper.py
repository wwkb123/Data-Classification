#!usr/bin/env python

import sys
import nltk


def extract_entity_names(t):
	if hasattr(t, 'label') and t.label:
		if t.label() == 'NE':  #Name Entity
			for child in t:
				value = 1
				print ("%s\t%d" % (child[0],value))
		else:
			for child in t:
				extract_entity_names(child)  #extract tree

for line in sys.stdin:
	line = line.strip()
	sentences = nltk.sent_tokenize(line)#each line as an element in the list
	tokenized_sentences = [nltk.word_tokenize(sentence) for sentence in sentences] #each word in the sentence as an element
	tagged_sentences = [nltk.pos_tag(sentence) for sentence in tokenized_sentences] #tag each word
	
	chunked_sentences = nltk.ne_chunk_sents(tagged_sentences, binary=True) #make a generator
	
	for tree in chunked_sentences:
		extract_entity_names(tree)
