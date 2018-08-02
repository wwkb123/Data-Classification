
#!/usr/bin/env python

import zipimport

importer = zipimport.zipimporter('seccure.mod')
seccure = importer.load_module('seccure')

def encrypt(plaintext,private_key):
	public_key = str(seccure.passphrase_to_pubkey(bytes(private_key.encode('utf-8'))))
	ciphertext = seccure.encrypt(bytes(plaintext.encode('utf-8')),bytes(public_key.encode('utf-8')))
	return ciphertext

def decrypt(ciphertext,private_key):
	plaintext = seccure.decrypt(ciphertext,bytes(private_key.encode('utf-8')))
	return plaintext

if __name__ == "__main__":
	print (decrypt(encrypt("tommy","mykey"),"mykey").decode('utf-8'))
