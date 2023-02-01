import pickle
import numpy as nm 
import sys
array=sys.argv[1]
a_list=array.split(",")
map_object = map(int, a_list)
list_of_integers = list(map_object)
integers=nm.array([list_of_integers])
 


loaded_model = pickle.load(open('C:/Users/nican/GitCAProject/Multiroom/ProjectServer/finalized_model.sav', 'rb'))
print(loaded_model.predict(integers))