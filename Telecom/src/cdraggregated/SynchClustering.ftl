
<#setting locale="en_US">

Figure \ref{${dir}} illustrates results using ${clustering} clustering mechanism. <#if (pca)> We applied PCA for reducing the features. PCA is typically very effective as the top 4 eigenvectors 
cover almost all the variability in data</#if>. Synchronization results are computed using ${distance} distance. 
We use a ${threshold} threshold to consider only those regions with enough data.
<#if (time_window == -1)>We considered all the time series to compute the distance.</#if>
<#if (time_window == 24)>We considered daily chunks of the time series to compute the distances.</#if>

\begin{figure}
\begin{center}
\includegraphics[width=1.0\columnwidth]{${dir}/boxplot-cluster-intra.png}
\includegraphics[width=0.45\columnwidth]{${dir}/clustering-deprivazione.png}
\includegraphics[width=0.45\columnwidth]{${dir}/clustering-redPC.png}
\includegraphics[width=0.31\columnwidth]{${dir}/clustering-assoc.png}
\includegraphics[width=0.31\columnwidth]{${dir}/clustering-blood.png}
\includegraphics[width=0.31\columnwidth]{${dir}/clustering-referendum.png}
\end{center}
\caption{Correlation results using ${clustering} clustering <#if (pca)> and PCA</#if>. Using ${distance} distance as synchronization. We use a ${threshold} threshold. 
<#if (time_window == -1)>We considered all the time series to compute the distance.</#if><#if (time_window == 24)>We considered daily chunks of the time series to compute the distances.</#if>}
\label{${dir}}
\end{figure} 


