import re
from gensim.parsing import strip_multiple_whitespaces

def remove_urls(text):
    return re.sub(r"http(s?)://[\S]+", '', text)

transform_to_lower = lambda s: s.lower()

def clean_code_pipe(code):
    code = remove_urls(code)
    code = transform_to_lower(code)
    code = strip_multiple_whitespaces(code)
    return code

def cleaning_pipe(doc):
    doc = remove_urls(doc)
    doc = transform_to_lower(doc)
    doc = strip_multiple_whitespaces(doc)
    return doc

def clean_texts(df, clean_code=True):
    for i, row in df.iterrows():
        df.loc[i, 'clean_comment'] = cleaning_pipe(row['comment'])
        df.loc[i, 'clean_type'] = cleaning_pipe(row['type'])
        df.loc[i, 'clean_detail'] = cleaning_pipe(row['detail'])
        if ('cmt_code' in row) and clean_code:
            df.loc[i, 'cmt_code'] = clean_code_pipe(row['cmt_code'])
        if ('vio_code' in row) and clean_code:
            df.loc[i, 'vio_code'] = clean_code_pipe(row['vio_code'])
        if ('raw_code' in row) and clean_code:
            df.loc[i, 'raw_code'] = clean_code_pipe(row['raw_code'])
    df['clean_type_detail'] = df['clean_type'] + ' ' + df['clean_detail']
    return df

def get_head_words(sentence, n):
    words = sentence.split()
    return ' '.join(words[:n])
