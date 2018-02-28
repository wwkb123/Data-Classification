
# coding: utf-8

# In[15]:


import random
for x in range(0,100):
    random.seed(x)
    first = random.randint(100,999)
    mid = random.randint(10,99)
    last = random.randint(1000,9999)
    ssn = str(first)+"-"+str(mid)+"-"+str(last)
    print(ssn)


# In[84]:


ssn = "510-28-3989"
phone = "917-444-8893"


# In[86]:


import re
condition = "(^\d{3}-?\d{2}-?\d{4}$|^XXX-XX-XXXX$)"
if(re.match(condition,ssn)):
    print("It is SSN")
else:
    print("It is not")

