import argparse
import json
import numpy as np
import os
import pandas as pd
import pickle
from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import StandardScaler
import sys
from acquisition import DEFAULT_LEVEL

from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.discriminant_analysis import QuadraticDiscriminantAnalysis
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression

from sklearn.naive_bayes import GaussianNB
from sklearn.neighbors import KNeighborsClassifier
from sklearn.svm import SVC
from sklearn.tree import DecisionTreeClassifier

from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score


# # Construct the argument parser
# ap = argparse.ArgumentParser()

# # Add the arguments to the parser
# ap.add_argument("-f", "--fingerprint", required=True, help="finerprint for each bssid")
# ap.add_argument("-p", "--path", required=True, help="path used to retrieve the model")
# args = vars(ap.parse_args())

# f = args['fingerprint']
# dataPath = args['path']
# #print(args['fingerprint'])

# fingerprint = json.loads(f)
# # sys.stderr.write(dataPath)
# # sys.stderr.write("\n")




dataPath = "C:/Users/bocca/Desktop/prove_modelli/"
# fingerprint = {"de:fe:23:2c:93:52":-93,"d8:0d:17:b7:38:22":-78,"28:3b:82:5c:ba:9b":-91,"c0:a3:6e:c3:e2:54":-61,"30:42:40:fd:66:82":-92,"28:3b:82:5c:ba:99":-85,"b0:0c:d1:05:97:04":-71,"1e:61:b4:a9:4f:e2":-80,"5c:96:9d:6a:19:ea":-68,"64:59:f8:e5:db:88":-88,"50:d4:f7:a4:47:61":-87,"9a:00:6a:d4:fe:e8":-79,"2c:91:ab:46:de:ee":-70,"3c:37:12:d1:36:2a":-44,"1c:61:b4:d9:4d:56":-75,"d8:0d:17:b7:38:21":-66,"1e:61:b4:a9:4c:9e":-85,"c0:a3:6e:c3:e2:55":-56,"1c:61:b4:d9:4f:e3":-83,"28:80:88:53:b4:33":-87,"dc:15:c8:3d:d6:00":-79,"1e:61:b4:a9:4f:e3":-84,"1e:61:b4:a9:4d:56":-77,"98:00:6a:c4:fe:e8":-77,"a4:cf:12:9c:9f:ed":-82,"5c:96:9d:6a:19:e9":-61,"2c:91:ab:46:de:ed":-65,"74:ac:b9:61:49:01":-90,"3c:37:12:d1:36:2b":-41}

# TESTARE QUESTO FINGERPRINT
fingerprint = {"d8:0d:17:b7:38:22":-82,"28:3b:82:5c:ba:9b":-82,"9a:80:bb:98:05:6e":-82,"c0:a3:6e:c3:e2:54":-65,"28:3b:82:5c:ba:99":-72,"b0:0c:d1:05:97:04":-61,"1c:61:b4:d9:4f:e2":-70,"1e:61:b4:a9:4f:e2":-70,"5c:96:9d:6a:19:ea":-70,"64:59:f8:e5:db:88":-90,"50:d4:f7:a4:47:61":-83,"9a:00:6a:d4:fe:e8":-82,"2c:91:ab:46:de:ee":-65,"3c:37:12:d1:36:2a":-42,"d8:0d:17:b7:38:21":-72,"1e:61:b4:a9:4c:9e":-85,"c0:a3:6e:c3:e2:55":-58,"28:80:88:53:b4:33":-82,"1e:61:b4:a9:4f:e3":-82,"40:b0:76:98:d4:cc":-93,"98:00:6a:c4:fe:e8":-85,"a4:cf:12:9c:9f:ed":-87,"5c:96:9d:6a:19:e9":-66,"2c:91:ab:46:de:ed":-71,"3c:37:12:d1:36:2b":-40}
fingerprint2 = {"d8:0d:17:b7:38:22":-84,"28:3b:82:5c:ba:9b":-81,"9a:80:bb:98:05:6e":-82,"c0:a3:6e:c3:e2:54":-64,"28:3b:82:5c:ba:99":-68,"b0:0c:d1:05:97:04":-64,"1c:61:b4:d9:4f:e2":-70,"1e:61:b4:a9:4f:e2":-70,"5c:96:9d:6a:19:ea":-68,"64:59:f8:e5:db:88":-87,"50:d4:f7:a4:47:61":-84,"9a:00:6a:d4:fe:e8":-75,"2c:91:ab:46:de:ee":-67,"3c:37:12:d1:36:2a":-45,"d8:0d:17:b7:38:21":-59,"1e:61:b4:a9:4c:9e":-85,"c0:a3:6e:c3:e2:55":-58,"28:80:88:53:b4:33":-82,"1e:61:b4:a9:4f:e3":-82,"40:b0:76:98:d4:cc":-93,"98:00:6a:c4:fe:e8":-85,"a4:cf:12:9c:9f:ed":-88,"5c:96:9d:6a:19:e9":-64,"2c:91:ab:46:de:ed":-72,"3c:37:12:d1:36:2b":-47}
fingerprint = fingerprint2
# # fingerprint_bagno = {'c0:3c:04:8e:6b:80': -81, 'dc:00:b0:86:85:f7': -47, 'dc:00:b0:86:85:f5': -52, '8c:97:ea:8f:ef:a4': -67, 'dc:00:b0:86:85:f6': -54, '7c:c1:77:aa:79:e0': -89, 'c0:d7:aa:62:0e:91': -54, 'c0:d7:aa:62:0e:8c': -68, '68:3f:7d:0d:7c:d5': -82, 'c0:3c:04:8e:6b:84': -85}
# # fingerprint_soggiorno = {'c0:3c:04:8e:6b:80': -67, 'dc:00:b0:86:85:f7': -55, 'dc:00:b0:86:85:f5': -63, '8c:97:ea:8f:ef:a4': -79, 'dc:00:b0:86:85:f6': -63, '7c:c1:77:aa:79:e0': -85, 'c0:d7:aa:62:0e:91': -40, 'c0:d7:aa:62:0e:8c': -54, '68:3f:7d:0d:7c:d5': -57, 'c0:3c:04:8e:6b:84': -65}
# # fingerprint = fingerprint_soggiorno
# open a file, where you stored the pickled data
file = open(os.path.join(dataPath, "model_std.pkl"), 'rb')

# dump information to that file
model = pickle.load(file)

# close the file
file.close()

file2 = open(os.path.join(dataPath, "model_LogisticRegression.pkl"), 'rb')

# dump information to that file
model2 = pickle.load(file2)

# close the file
file.close()

#dataPath = "C:/Users/bocca/Desktop/csv/"
# Load the dataset
df = pd.read_csv(os.path.join(dataPath, "dataset_raw.csv"))


x_train = pd.read_csv(os.path.join(dataPath, "x_train.csv"))
x_train_std = pd.read_csv(os.path.join(dataPath, "x_train_std.csv"))
y_train = pd.read_csv(os.path.join(dataPath, "y_train.csv"))

x_test_std = pd.read_csv(os.path.join(dataPath, "x_test_std.csv"))
y_test = pd.read_csv(os.path.join(dataPath, "y_test.csv"))

std_scaler = StandardScaler()
std_scaler = std_scaler.fit(x_train)
# print(x_train.columns)
# print(std_scaler.scale_)
# print(std_scaler.mean_)
# x_train = std_scaler.fit_transform(x_train)    
# x_test = std_scaler.transform(x_test)
# print(x_train_std.shape)

#model.fit(X_train, Y_train)

# fromDict = pd.DataFrame.from_dict(fingerprint)
# print(fromDict)


# RICORDARSI
# ORA BISOGNA AGGIUNGERE TUTTI GLI ACCESS POINT, ANCHE QUELLI CHE NON PRENDONO CON -99
target = df.target
df = df.drop(columns=['target'])

final = pd.DataFrame()
for i in range(0, len(df.columns)) :
    print(df.columns[i])
    # print(df.columns[i])
    # print(fingerprint.keys() )
    if df.columns[i] in fingerprint.keys() :
        
        print([fingerprint[df.columns[i]]])
        final[df.columns[i]] = [fingerprint[df.columns[i]]]
    else :
        print([DEFAULT_LEVEL])
        final[df.columns[i]] = [DEFAULT_LEVEL]

# sys.stderr.write("POST DF CREATION")
# sys.stderr.write("\n")
print(final)
print(final.shape)
print(final.columns)
# print(df)
# print(df.shape)
# print(x_train.shape)

# fingerprint_2 = "1.2443272485463936,0.8952302758813939,1.3988735162578092,-0.9814887326643434,-0.4996394769957732,-0.7998079083132741,0.22623069732562087,-0.9020905076306504,-0.713520950975272,-0.28697001320034893"
# f_list = fingerprint_2.split(",")
# print(f_list)
# map_object = map(float, f_list)
# print(map_object)
# list_of_integers = list(map_object)
# print(list_of_integers)


#print(fingerprint)
# print(final)
# integers = np.array([final])
# print(integers)
floats = std_scaler.transform(final)
# print(floats)
model.fit(x_train_std, y_train)
model2.fit(x_train_std, y_train)
# print(x_train_std)
# print(model.predict(floats))
# print(model.predict_proba(floats))



# Creating a instance of label Encoder.
le = LabelEncoder()
# print(target)
# Using .fit_transform function to fit label encoder and return encoded label
label = le.fit_transform(target)
# print(label)
# print(model.predict(floats))
# print(type(model.predict_proba(floats)))
# rounded_integer_array = floats.astype(int)
rounded_integer_array = (np.rint(model.predict(floats))).astype(int)
# sys.stderr.write("SIAMO ALLA FINE ")
# print(rounded_integer_array)
# print(le.inverse_transform(rounded_integer_array))
sys.stdout.write(le.inverse_transform(rounded_integer_array)[0])
# sys.stdout.write(le.inverse_transform(model.predict(floats))[0])
sys.stdout.flush()


# ANALISI
predictions = model.predict(x_test_std)
print()
print(accuracy_score(y_test, predictions))
print(confusion_matrix(y_test, predictions))
print(classification_report(y_test, predictions))

# ANALISI 2
predictions = model2.predict(x_test_std)
print()
print(accuracy_score(y_test, predictions))
print(confusion_matrix(y_test, predictions))
print(classification_report(y_test, predictions))


############################################################################################################


# models = [LogisticRegression(), GaussianNB(), QuadraticDiscriminantAnalysis(), LinearDiscriminantAnalysis(), KNeighborsClassifier(), SVC(), DecisionTreeClassifier(), RandomForestClassifier()]

# for model in models :
#     model_name = "model_" + type(model).__name__ + ".pkl"
#     print("\n")
#     print(type(model).__name__)
#     print("\n")
#     print(model_name)
    

#     file = open(os.path.join(dataPath, model_name), 'rb')

#     # dump information to that file
#     model = pickle.load(file)

#     # close the file
#     file.close()

#     model.fit(x_train_std, y_train)

    
#     rounded_integer_array = model.predict(floats)
    
#     print("\n")
#     print(rounded_integer_array)
#     rounded_integer_array = (np.rint(rounded_integer_array)).astype(int)
#     print("\n")
#     print(rounded_integer_array)
#     print("\n")
#     print(le.inverse_transform(rounded_integer_array)[0])
#     print("---------------------------------------------------------------------------")

time_spent = pd.read_csv(os.path.join("C:/Users/bocca/Desktop/laurea_magistrale_informatica/SCA/progetto/Context-Aware-Project-Multiroom-Localization/SLAC/maven/server", "time_spent.csv"))
print(time_spent)
print(time_spent["time_spent"].mean())
print(time_spent["channel"].mean())