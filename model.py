import numpy as np
from keras.models import Model
import sys
import json

model = None

def send(message):
	print(message, flush=True)

def init(payload):
	try:
		model = Model.from_config(payload['model'])
		model.set_weights([np.array(w) for w in payload['weights']])
		model.compile(loss='binary_crossentropy', optimizer='adam')
		send('INIT: true')
	except Exception as ex:
		send('INIT: Failed to load model/weights')

def predict(payload):
	result = -1
	if model is not None:
		x = {i: np.array(x[i]) for i in payload}
		result = model.predict(x).tolist()[0][0]
	send('PREDICT: %s' % str(result))


while True:
	i = input()
	command, payload = i.split(': ')
	try:
		payload = json.loads(payload)
	except:
		send(command.upper() + ': Failed to parse')
		pass

	if command == 'init':
		init(payload)
	elif command == 'predict':
		predict(payload)