import time
with open("output.txt") as file:
	content = file.read()

freqTable = {}

start_time = time.time()

count = 0
for word in content.split('\t'):
	count+=1
	if word in freqTable:
		freqTable[word] = freqTable[word]+1
	else:
		freqTable[word] = 1

elapsed_time = time.time() - start_time

output = open("freqTable.txt", "w")
for word,freq in freqTable.items():
	output.write(str(word)+" "+str(freq)+" ")

print("Finished. The time is ",elapsed_time,count)