

if __name__ == '__main__':
    fin = open("faa-stream.csv")
    fout = open("faa-stream2.csv", "w")
    i = 0
    for line in fin:
        
        parts = line.split(",")

        fout.write(",".join(parts[:6]) + "\n")


    fin.close()
    fout.close()
