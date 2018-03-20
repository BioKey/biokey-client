import numpy as np
from keras.models import Model
from keras.models import load_model
import sys
import json

model = None

def send(message):
	print(message, flush=True)

def init(payload):
	global model
	try:
		model = Model.from_config(payload['model'])
		model.set_weights([np.array(w) for w in payload['weights']])
		model.compile(loss='binary_crossentropy', optimizer='adam')
		send('INIT: true')
	except Exception as ex:
		send('INIT: Failed to load model/weights')

def predict(payload):
	global model
	result = -1
	if model is not None:
		try:
			x = {i: np.array([payload[i]]) for i in payload}
			result = model.predict(x).tolist()[0][0]
		except Exception as e:
			sys.stderr.write(e)
	else:
		sys.stderr.write("PREDICT: Model not defined")
	send('PREDICT: %s' % str(result))


while True:
	i = input()
	try:
		command, payload = i.split(': ', 1)
		payload = json.loads(payload)
		if command == 'init':
			init(payload)
		elif command == 'predict':
			predict(payload)
	except:
		send(command.upper() + ': Failed to parse')

	