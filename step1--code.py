#!/usr/bin/env python2
# -*- coding: utf-8 -*-
"""
Created on Fri Aug 11 17:02:44 2017

"""
from pandas import Series

import pandas as pd

path = '/Users/huangjingyi/Desktop/'
data = pd.read_csv (path+'000001.csv')  #,encoding='gb2312')
#High=data['High'][0:100]
#Close=data['Close'][0:100]
#Low=data['Low'][0:100]

#print (Close[99])

#Exponential Moving Average
def EMA(df, n):
    EMA = Series(df['Close'].ewm(span = n, min_periods = n - 1).mean(), name = 'EMA_' + str(n)) 
    df = df.join(EMA)
    return df


#MACD, MACD Signal and MACD difference
def MACD(df, n_fast, n_slow):
    EMAfast = Series(df['Close'].ewm(span = n_fast, min_periods = n_slow - 1).mean())
    EMAslow = Series(df['Close'].ewm(span = n_slow, min_periods = n_slow - 1).mean())
    MACD = Series(EMAfast - EMAslow, name = 'MACD_' + str(n_fast) + '_' + str(n_slow))
    MACDsign = Series(MACD.ewm(span = 9, min_periods = 8).mean(), name = 'MACDsign_' + str(n_fast) + '_' + str(n_slow))
    MACDdiff = Series(MACD - MACDsign, name = 'MACDdiff_' + str(n_fast) + '_' + str(n_slow))
    df = df.join(MACD)
    df = df.join(MACDsign)
    df = df.join(MACDdiff)
    return df

#Relative Strength Index
def RSI(df, n):
    i = 0
    UpI = [0]
    DoI = [0]
    while i + 1 <= df.index[-1]:
        UpMove = df.get_value(i + 1, 'High') - df.get_value(i, 'High')
        DoMove = df.get_value(i, 'Low') - df.get_value(i + 1, 'Low')
        if UpMove > DoMove and UpMove > 0:
            UpD = UpMove
        else: UpD = 0
        UpI.append(UpD)
        if DoMove > UpMove and DoMove > 0:
            DoD = DoMove
        else: DoD = 0
        DoI.append(DoD)
        i = i + 1
    UpI = Series(UpI)
    DoI = Series(DoI)
    PosDI = Series(UpI.ewm(span = n, min_periods = n - 1).mean())
    NegDI = Series(DoI.ewm(span = n, min_periods = n - 1).mean())
    RSI = Series(PosDI / (PosDI + NegDI), name = 'RSI_' + str(n))
    df = df.join(RSI)
    print df
    return df

#####################


print(MACD(data,12,26))




recording=data
recording['RSI_10']=RSI(data,10)['RSI_10']
recording['RSI_5']=RSI(data,5)['RSI_5']
recording['DIF']=MACD(data,12,26)['MACD_12_26']
recording['MACD']=MACD(data,12,26)['MACD_12_26']





recording.to_csv('result.csv')































