# !/usr/bin/env python
# -*- coding: utf-8 -*-
"""Model server with some hardcoded paths."""

from flask.views import MethodView

from flask import Flask, request, jsonify
from gevent.pywsgi import WSGIServer
import numpy as np
from keras.models import Model

app = Flask(__name__)
model = None

'''
def get_model(model):
    return {
        'model': model.get_config(),
        'weights': [w.tolist() for w in model.get_weights()]
    }
'''

class ModelLoader(MethodView):
    """ModelLoader class initialzes the model params and waits for a post request to server predictions."""

    def __init__(self):
        """Initialize ModelLoader class."""
        pass

    def post(self):
        global model
        """Accept a post request to serve predictions."""
        model_def = request.get_json()
        try:
            model = Model.from_config(model_def['model'])
            model.set_weights([np.array(w) for w in model_def['weights']])
            model.compile(loss='binary_crossentropy', optimizer='adam')
        except Exception as ex:
            return 'Failed to load model/weights'
        return 'Success'

class Predictor(MethodView):
    """ModelLoader class initialzes the model params and waits for a post request to server predictions."""

    def __init__(self):
        """Initialize ModelLoader class."""
        pass

    def post(self):
        global model
        """Accept a post request to serve predictions."""
        content = request.get_json()
        x = content['inputs']
        x = {i: np.array(x[i]) for i in x}
        pred_val = model.predict(x).tolist()[0][0]
        return jsonify({'prediction': pred_val})

# Port HOSH aka 4674
def run(host='0.0.0.0', port=4674):
    """Run a WSGI server using gevent."""
    app.add_url_rule('/init', view_func=ModelLoader.as_view('init'))
    app.add_url_rule('/predict', view_func=Predictor.as_view('predict'))
    print('running server http://{0}'.format(host + ':' + str(port)))
    WSGIServer((host, port), app).serve_forever()

run()