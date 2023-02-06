import configparser
import numpy as nm
import pickle
import sys


config = configparser.ConfigParser()
config.read("config.ini")


array = sys.argv[1]
a_list = array.split(",")
map_object = map(int, a_list)
list_of_integers = list(map_object)
integers = nm.array([list_of_integers])
 

loaded_model = pickle.load(open(config["ml"]["MODEL_FILE_DIRECTORY"] + config["ml"]["MODEL_FILE"], 'rb'))
print(loaded_model.predict(integers))