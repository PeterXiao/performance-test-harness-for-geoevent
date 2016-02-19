
import json

if __name__ == '__main__':
    fin = open("faa-stream.csv")

    fout = open("faa-stream2.txt", "w")
    
    jsonLine = {}

    for line in fin:
        parts = line.split(",")

        # pick the line you want and set the field names

        jsonLine["id"] = int(parts[1])
        #print parts[1]

        jsonLine["tn"] = parts[2]
        #print parts[2]

        jsonLine["dtg"] = parts[3]
        #print parts[3]

        jsonLine["lon"] = float(parts[4])
        #print parts[4]

        jsonLine["lat"] = float(parts[5])
        #print parts[5]


        #print json.dumps(jsonLine)
        #break

        fout.write(json.dumps(jsonLine) + "\n")

    fin.close()
    fout.close()
