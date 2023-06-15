import os
import pandas as pd
import numpy as np
import math
from sklearn.decomposition import PCA
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import StandardScaler



def data_preprocessing(dataPath) :
    # Load the dataset
    df = pd.read_csv(os.path.join(dataPath, "dataset_raw.csv"))

    # Creating a instance of label Encoder.
    le = LabelEncoder()
    
    # Using .fit_transform function to fit label encoder and return encoded label
    label = le.fit_transform(df['target'])
    

    # removing the column 'Purchased' from df
    # as it is of no use now.
    df.drop("target", axis=1, inplace=True)

    columns = list(df.columns)
    
    # Appending the array to our dataFrame
    # with column name 'Purchased'
    df["target"] = label
    



    test_size = 0.15
    #val_size = 0.15
    seed = 5

    # get features and target from dataset
    target = df.target
    features = df.drop(['target'], axis=1)
    df = df.to_numpy()
    n_samples, n_features = df.shape
    n_features -= 1
    features = df[0:n_samples, 0:n_features]
    target = df[0:n_samples, n_features]

    x_train, x_test, y_train, y_test = train_test_split(features, target, test_size=test_size)
    df_x_train = pd.DataFrame(data=x_train, columns=columns)
    df_y_train = pd.DataFrame(data=y_train, columns=['target'])
    df_x_test = pd.DataFrame(data=x_test, columns=columns)
    df_y_test = pd.DataFrame(data=y_test, columns=['target'])
    df_x_train.to_csv(os.path.join(dataPath, "x_train.csv"), index=False)
    df_y_train.to_csv(os.path.join(dataPath, "y_train.csv"), index=False)
    df_x_test.to_csv(os.path.join(dataPath, "x_test.csv"), index=False)
    df_y_test.to_csv(os.path.join(dataPath, "y_test.csv"), index=False)


    std_scaler = StandardScaler()
    std_scaler.fit(x_train)
    x_train_std = std_scaler.fit_transform(x_train)
    x_test_std = std_scaler.transform(x_test)

    pd.DataFrame(data=x_train_std, columns=columns).to_csv(os.path.join(dataPath, "x_train_std.csv"), index=False)
    pd.DataFrame(data=x_test_std, columns=columns).to_csv(os.path.join(dataPath, "x_test_std.csv"), index=False)
    

