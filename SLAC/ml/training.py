import configparser
import glob
import numpy as nm
import os
import pandas as pd
from pandas import read_csv
import pickle
from sklearn.model_selection import train_test_split
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score
from sklearn.linear_model import LogisticRegression
from sklearn.neighbors import KNeighborsClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestClassifier

config = configparser.ConfigParser()
config.read("config.ini")


data_set = pd.DataFrame()
# INSERIRE QUI LA FASE DI COMBINAZIONE DEI VARI CSV IN UNO UNICO CHIAMATO Dataset.csv
csv_files = os.listdir(config["ml"]["MEASUREMENT_DIRECTORY"])
for file in csv_files :
            df_csv = pd.read_csv(file)
            data_set = data_set.append(df_csv, ignore_index = True)

data_set.to_csv(config["ml"]["DATASET_DIRECTORY"] + config["ml"]["DATASET_FILE"])



data_set = pd.read_csv(config["ml"]["DATASET_DIRECTORY"] + config["ml"]["DATASET_FILE"], header = None, delimiter = ";", skiprows = 1)  
data_set = data_set.to_numpy()
n_samples, n_features = data_set.shape
n_features -= 1
X = data_set[0:n_samples, 0:n_features]
Y = data_set[0:n_samples, n_features]


from sklearn.model_selection import train_test_split  
X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size = 0.2, random_state = 4)


""" from matplotlib import pyplot
models = []
models.append(('LR', LogisticRegression(solver='liblinear', multi_class='ovr')))
models.append(('LDA', LinearDiscriminantAnalysis()))
models.append(('KNN', KNeighborsClassifier()))
models.append(('CART', DecisionTreeClassifier()))
models.append(('NB', GaussianNB()))
models.append(('SVM', SVC(gamma='auto')))
results = []
names = []

for name, model in models:
	kfold = StratifiedKFold(n_splits=10, random_state=1, shuffle=True)
	cv_results = cross_val_score(model, X, Y, cv=kfold, scoring='accuracy')
	results.append(cv_results)
	names.append(name)
	print('%s: %f (%f)' % (name, cv_results.mean(), cv_results.std()))

pyplot.boxplot(results, labels=names)
pyplot.title('Algorithm Comparison')
pyplot.show() """


st_x = StandardScaler()
st_x.fit(X_train) 
X_train = st_x.fit_transform(X_train)    
X_test = st_x.transform(X_test)
model = KNeighborsClassifier(n_neighbors = 5)
model.fit(X_train, Y_train)


""" filename = 'finalized_model.sav'
pickle.dump(model, open(filename, 'wb')) 
loaded_model = pickle.load(open('C:/Users/nican/GitCAProject/Multiroom/ProjectServer/finalized_model.sav', 'rb'))
"""

""" predictions = model.predict(X_test)
print(accuracy_score(Y_test, predictions))
print(confusion_matrix(Y_test, predictions))
print(classification_report(Y_test, predictions)) """


""" error = []

for i in range(1, 40):
    knn = KNeighborsClassifier(n_neighbors=i)
    knn.fit(X_train, Y_train)
    pred_i = knn.predict(X_test)
    error.append(nm.mean(pred_i != Y_test))

pyplot.figure(figsize=(12, 6))
pyplot.plot(range(1, 40), error, color='red', linestyle='dashed',
 marker='o', markerfacecolor='blue', markersize=10)
pyplot.title('Error Rate K Value')
pyplot.xlabel('K Value')
pyplot.ylabel('Mean Error')
pyplot.show() """


import sys
array = sys.argv[1]
a_list = array.split(";")
map_object = map(int, a_list)
list_of_integers = list(map_object)
integers = nm.array([list_of_integers])
integers = st_x.transform(integers)
print(model.predict(integers))