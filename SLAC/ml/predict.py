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


# Construct the argument parser
ap = argparse.ArgumentParser()

# Add the arguments to the parser
ap.add_argument("-f", "--fingerprint", required=True, help="finerprint for each bssid")
ap.add_argument("-p", "--path", required=True, help="path used to retrieve the model")
args = vars(ap.parse_args())

f = args['fingerprint']
dataPath = args['path']

fingerprint = json.loads(f)

file = open(os.path.join(dataPath, "model_std.pkl"), 'rb')

# dump information to that file
model = pickle.load(file)

# close the file
file.close()

# Load the dataset
df = pd.read_csv(os.path.join(dataPath, "dataset_raw.csv"))


x_train = pd.read_csv(os.path.join(dataPath, "x_train.csv"))
x_train_std = pd.read_csv(os.path.join(dataPath, "x_train_std.csv"))
y_train = pd.read_csv(os.path.join(dataPath, "y_train.csv"))

std_scaler = StandardScaler()
std_scaler = std_scaler.fit(x_train)

target = df.target
df = df.drop(columns=['target'])

final = pd.DataFrame()
for i in range(0, len(df.columns)) :
    if df.columns[i] in fingerprint.keys() :
        final[df.columns[i]] = [fingerprint[df.columns[i]]]
    else :
        final[df.columns[i]] = [DEFAULT_LEVEL]


floats = std_scaler.transform(final)
model.fit(x_train_std, y_train)

le = LabelEncoder()
label = le.fit_transform(target)
rounded_integer_array = (np.rint(model.predict(floats))).astype(int)
sys.stdout.write(le.inverse_transform(rounded_integer_array)[0])
sys.stdout.flush()


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