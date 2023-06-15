import pandas as pd
import os

DEFAULT_LEVEL = -99

def data_acquisition(rpPath) :
    data_set = pd.DataFrame()
    csv_files = os.listdir(rpPath)
    for file in csv_files :
        suffix = file.rfind(".csv")
        if suffix != -1 and (file == "bagno.csv" or file == "camera.csv" or file == "soggiorno.csv") :
            filename = file[:suffix]

            df_csv = pd.read_csv(os.path.join(rpPath, file))
            df_csv.drop_duplicates(inplace=True)

            
            df_csv = df_csv.assign(target=filename)
            data_set = data_set.append(df_csv, ignore_index=True)
            data_set = data_set.fillna(DEFAULT_LEVEL)

    target = data_set["target"]
    data_set = data_set.drop(columns=["target"])
    data_set["target"] = target

    data_set.to_csv(os.path.join(rpPath, "dataset_raw.csv"), index=False)