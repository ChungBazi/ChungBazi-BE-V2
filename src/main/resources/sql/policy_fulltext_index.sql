-- 정책 본 검색에서 사용하는 MySQL ngram FULLTEXT 인덱스.
-- 적용 전 동일한 이름의 인덱스가 존재하는지 확인한 뒤 한 번만 실행한다.
ALTER TABLE policy
ADD FULLTEXT INDEX ft_policy_search (title, summary, support_content)
WITH PARSER ngram;