import argparse
import os
import pandas as pd
import pickle

from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.discriminant_analysis import QuadraticDiscriminantAnalysis
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression

from sklearn.model_selection import GridSearchCV
from sklearn.model_selection import StratifiedKFold

from sklearn.naive_bayes import GaussianNB
from sklearn.neighbors import KNeighborsClassifier
from sklearn.svm import SVC
from sklearn.tree import DecisionTreeClassifier



from sklearn.metrics import mean_squared_error
from sklearn.metrics import mean_absolute_error
from sklearn.metrics import r2_score

from sklearn.model_selection import cross_val_score

import matplotlib.pyplot as plt

from acquisition import data_acquisition
from preprocessing import data_preprocessing


# Construct the argument parser
ap = argparse.ArgumentParser()

# Add the arguments to the parser
ap.add_argument("-p", "--path", required=True, help="path used to save the model")
args = vars(ap.parse_args())

path = args['path']
print(args['path'])


data_acquisition(path)
data_preprocessing(path)



# # Dictionary with each parameters to tune for each model to run
# param_grid = {
#     'LogisticRegression': {
#         'random_state': [20], 'solver': ['liblinear'], 'multi_class': ['ovr']
#     },
#     'GaussianNB': {
#     },
#     'QuadraticDiscriminantAnalysis': {
#         'store_covariance': [True]
#     },
#     'LinearDiscriminantAnalysis': {
#         'n_components': [2]
#     },
#     'KNeighborsClassifier': {
#         'n_neighbors': [3]
#     },
#     'SVC': {
#         'gamma': ['auto'], 'kernel': ['linear'], 'C': [1000]
#     },
#     'DecisionTreeClassifier': {
#         'max_depth': [4], 'criterion': ['gini'], 'ccp_alpha': [0.0035]
#     },
#     'RandomForestClassifier': {
#         'n_estimators': [1000]
#     }
# }

#models = [LogisticRegression(), GaussianNB(), QuadraticDiscriminantAnalysis(), LinearDiscriminantAnalysis(), KNeighborsClassifier(), SVC(), DecisionTreeClassifier(), RandomForestClassifier()]
models = [LogisticRegression(random_state=20, solver='liblinear', multi_class='ovr'), GaussianNB(), QuadraticDiscriminantAnalysis(store_covariance=True), LinearDiscriminantAnalysis(n_components=2), KNeighborsClassifier(n_neighbors=3), SVC(gamma='auto', kernel='linear', C=1000), DecisionTreeClassifier(max_depth=4, criterion="gini", ccp_alpha=0.0035), RandomForestClassifier(n_estimators=1000)]



best_params = {type(model).__name__ : [] for model in models}
n_iter = 10
cv = 2


x_train = pd.read_csv(os.path.join(path, "x_train.csv")).to_numpy()
x_train_std = pd.read_csv(os.path.join(path, "x_train_std.csv")).to_numpy()
y_train = pd.read_csv(os.path.join(path, "y_train.csv")).to_numpy()
x_test = pd.read_csv(os.path.join(path, "x_test.csv")).to_numpy()
y_test = pd.read_csv(os.path.join(path, "y_test.csv")).to_numpy()
print(x_train)
print(y_train)


names = []
results = []
max = -1
final_model = None
for model in models :
    kfold = StratifiedKFold(n_splits=2, random_state=1, shuffle=True)
    cv_results = cross_val_score(model, x_train_std, y_train, cv=kfold, scoring='accuracy')
    results.append(cv_results)
    names.append(type(model).__name__)
    print('%s: %f (%f)' % (type(model).__name__, cv_results.mean(), cv_results.std()))


    # filename = 'model_' + type(model).__name__ + '.pkl'
    # modelPath = os.path.join(path, filename)
    # # open a file, where you ant to store the data
    # file = open(modelPath, 'wb')

    # pickle.dump(model, file)

    # # close the file
    # file.close()


    if cv_results[0] > max :
        max = cv_results[0]
        final_model = model


plt.boxplot(results, labels=names)
plt.title('Algorithm Comparison')
plt.show()



filename = 'model_std.pkl'
modelPath = os.path.join(path, filename)
# open a file, where you ant to store the data
file = open(modelPath, 'wb')

pickle.dump(final_model, file)

# close the file
file.close()