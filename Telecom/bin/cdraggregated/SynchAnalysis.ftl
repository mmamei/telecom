
<#setting locale="en_US">

Figure \ref{${dir}} illustrates results with ${type} approach. 
Synchronization results are computed using ${distance} distance. 
We use a ${threshold} threshold to consider only those regions with enough data.
<#if (time_window == -1)>We considered all the time series to compute the distance.</#if>
<#if (time_window == 24)>We considered daily chunks of the time series to compute the distances.</#if>

\begin{figure}
\begin{center}
\includegraphics[width=1.0\columnwidth]{${dir}/boxplot-prov2011.png}
\includegraphics[width=0.31\columnwidth]{${dir}/deprivazione.png}
\includegraphics[width=0.31\columnwidth]{${dir}/redPC.png}
\includegraphics[width=0.31\columnwidth]{${dir}/redPCP.png}

\includegraphics[width=0.24\columnwidth]{${dir}/assoc.png}
\includegraphics[width=0.24\columnwidth]{${dir}/blood.png}
\includegraphics[width=0.24\columnwidth]{${dir}/referendum.png}
\includegraphics[width=0.24\columnwidth]{${dir}/soccap.png}

\includegraphics[width=0.24\columnwidth]{${dir}/Unemployed-Pop.png}
\includegraphics[width=0.24\columnwidth]{${dir}/Working-Pop.png}
\includegraphics[width=0.24\columnwidth]{${dir}/Pop-illiterate.png}
\includegraphics[width=0.24\columnwidth]{${dir}/Pop-university.png}
\end{center}
\caption{Correlation results with ${type} approach. Using ${distance} distance as synchronization. We use a ${threshold} threshold. 
<#if (time_window == -1)>We considered all the time series to compute the distance.</#if><#if (time_window == 24)>We considered daily chunks of the time series to compute the distances.</#if>}
\label{${dir}}
\end{figure} 


