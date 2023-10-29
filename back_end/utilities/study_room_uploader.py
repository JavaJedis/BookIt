"""
Study Room Data Uploader for Bookit by Java-Jedis
"""

#Library imports
from distutils.command import build
import pymongo, json

#Connect to mongodb
client = pymongo.MongoClient("mongodb://127.0.0.1:27017")
db = client["study_room_db"]

#Process study room file
s_r_file = open("./back_end/utilities/data/study_rooms.json")
raw_data = s_r_file.read()
data = json.loads(raw_data)

#Upload list of buildings into db
building_list_col = db["all_sr_building"]
for building in data:
    
    building_data = {
        "building_code": building["building_code"], 
        "building_name": building["building"],
        "building_address": building["address"], 
        "open_times": building["open_time"], 
        "close_times": building["close_time"]
    }
    building_list_col.insert_one(building_data)
    
    building_coll = db[building["building_code"]]
    building_data_detailed = {
        "building_code": building["building_code"], 
        "building_name": building["building"],
        "building_address": building["address"], 
        "open_times": building["open_time"], 
        "close_times": building["close_time"], 
        "rooms": building["rooms"]
    }
    
    building_coll.insert_one(building_data_detailed)
    
client.close()
    


