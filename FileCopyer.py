with open('output.txt') as file:
# 	content = file.read()
	line = file.readlines()

# output = open("output.txt", "a")

# for x in range(0,100):
# 	output.write(content)

print("finished", len(line))