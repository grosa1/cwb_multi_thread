import sys
import os


parent_f = sys.argv[1]

with open(os.path.join(parent_f, 'cwb_result.csv'), 'w') as out_file:
    out_file.write('RULE,USER,EXPECTED_RESULT,CURRENT_RESULT,CHECK\n')
    for res in os.listdir(parent_f):
        current = os.path.join(parent_f, res)
        if os.path.isfile(current) and current.endswith('.txt'):
            with open(current, 'r') as infile:
                current_result = 'TRUE' if 'TRUE' in infile.read() else 'FALSE'
                
                filename = res.split('.')[0].split('_')

                current_rule = filename[2][1:]
                current_user = filename[1][1:]

                expected_result = 'TRUE' if current_rule == current_user else 'FALSE'
                check = 'OK' if current_result == expected_result else 'ERROR'

                out_file.write(current_rule + ',' + current_user + ',' + expected_result + ',' + current_result + ',' + check + '\n')


